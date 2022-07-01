package org.github.mlb.content.biz.article.service.impl;

import org.github.mlb.content.article.convert.ArticleConvert;
import org.github.mlb.content.article.entity.ArticleEntity;
import org.github.mlb.content.article.param.AddArticleParam;

import org.github.mlb.content.biz.article.mapper.ArticleMapper;
import org.github.mlb.content.category.entity.CategoryEntity;
import org.github.mlb.content.biz.article.manager.ArticleManager;
import org.github.mlb.content.biz.article.service.ArticleService;
import org.github.mlb.content.biz.category.manger.CategoryManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;


/**
 * @author JiHongYuan
 * @date 2021/9/15 21:46
 */
@Service
@AllArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleManager articleManager;
    private final ArticleMapper articleMapper;
    private final CategoryManager categoryManager;

    @Override
    public List<Long> listIdByUserId(Long userId) {
        return articleMapper.selectListIdByUserId(userId);
    }

    @Override
    public ArticleEntity add(AddArticleParam param) {
        CategoryEntity category = categoryManager.getById(param.getCategoryId());
        Assert.notNull(category, "数据异常！");

        ArticleEntity article = ArticleConvert.INSTANCE.toEntity(param);
        article.setCategoryId(category.getId());
        article.setRepositoryId(category.getRepositoryId());
        article.setNewVersion(0);
        articleManager.add(article);
        return article;
    }

    @Override
    public void update(AddArticleParam addArticleParam) {
        // TODO
    }

}
