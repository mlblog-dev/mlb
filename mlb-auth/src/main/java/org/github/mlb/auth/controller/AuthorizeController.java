package org.github.mlb.auth.controller;

import org.github.mlb.auth.component.JwtProvider;
import org.github.mlb.auth.template.GithubAuthorize;
import org.github.mlb.auth.user.entity.UserEntity;
import org.github.mlb.auth.user.service.UserService;
import org.github.mlb.common.model.UserInfo;
import org.github.mlb.common.utils.JwtUtil;
import org.github.mlb.common.utils.Result;
import lombok.AllArgsConstructor;
import org.github.mlb.content.api.ContentUserDataApi;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author JiHongYuan
 * @date 2021/9/22 22:16
 */
@AllArgsConstructor
@RestController
@RequestMapping
public class AuthorizeController {

    private RedissonClient redissonClient;

    private final UserService userService;
    private final GithubAuthorize githubAuthorize;
    private final JwtProvider jwtProvider;

    private final ContentUserDataApi contentUserDataApi;

    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        response.sendRedirect(githubAuthorize.getRedirectUrl());
    }

    @GetMapping("/oauth/callback")
    public Result<String> callback(HttpServletRequest request) {
        UserInfo userInfo = githubAuthorize.authorize(request);
        userInfo.setCurrentAt(new Date());
        UserEntity userEntity = Optional.ofNullable(userService.getByAccess(userInfo.getAccess()))
                // 新增用户
                .orElseGet(() -> {
                    UserEntity user = new UserEntity();
                    user.setAccess(userInfo.getAccess());
                    user.setCreateAt(new Date());
                    userService.save(user);
                    return user;
                });

        userInfo.setId(userEntity.getId());
        userInfo.setStatus(userEntity.getStatus());

        String authorizeToken = jwtProvider.generateToken();
        String token = authorizeToken.substring(JwtUtil.TOKEN_PREFIX.length());
        String msg = redissonClient.<String>getBucket("auth:" + userEntity.getId()).getAndSet(token, jwtProvider.getProperties().getExpirationSecond(), TimeUnit.SECONDS);
        if (msg != null) {
            // 删除旧的token
            redissonClient.getBucket("auth:userInfo:" + msg).delete();
        }

        setUserInfo(userInfo, authorizeToken);
        redissonClient.getBucket("auth:userInfo:" + token).set(userInfo, jwtProvider.getProperties().getExpirationSecond(), TimeUnit.SECONDS);
        return Result.ofSuccess(authorizeToken);
    }

    private void setUserInfo(UserInfo userInfo, String authorizeToken) {
        // 仓库
        Result<List<Long>> repositoryResult = contentUserDataApi.listRepositoryByUserId(userInfo.getId(), authorizeToken);
        if (repositoryResult.isSuccess()) {
            userInfo.setRepositories(new HashSet<>(repositoryResult.getData()));
        }

        // 分类
        Result<List<Long>> categoryResult = contentUserDataApi.listCategoryByUserId(userInfo.getId(), authorizeToken);
        if (categoryResult.isSuccess()) {
            userInfo.setCategories(new HashSet<>(categoryResult.getData()));
        }

        // 博文
        Result<List<Long>> articleResult = contentUserDataApi.listArticleByUserId(userInfo.getId(), authorizeToken);
        if (articleResult.isSuccess()) {
            userInfo.setArticles(new HashSet<>(articleResult.getData()));
        }
    }
}