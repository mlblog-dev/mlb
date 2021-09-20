package com.github.mlb.content.biz.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.mlb.content.biz.article.entity.ArticleEntity;
import com.github.mlb.content.biz.article.request.ArticleRequest;

/**
 * @author JiHongYuan
 * @date 2021/9/15 21:45
 */
public interface ArticleService extends IService<ArticleEntity> {

    void add(ArticleRequest articleRequest);

    void update(ArticleRequest articleRequest);

}