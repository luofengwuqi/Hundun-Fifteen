package com.createpen.fifteenhundun.module.article.service;

import com.createpen.fifteenhundun.module.article.pojo.DTO.InsertArticleDTO;
import com.createpen.fifteenhundun.module.article.pojo.Entity.Article;

import java.util.List;

public interface ArticleService {
    List<Article> selectArticleList(Integer Page);

    void insertArticle(InsertArticleDTO dto);

    void updateArticleGood(Long articleId);

    void updateArticleBad(Long articleId);

    void syncRedisToMysqlForCount();

    //同步redis的文章用户点赞关系到数据库
    void syncRedisToMysqlForArticleGoodUser();

    void syncRedisToMysqlForArticleBadUser();

    void deleteArticleById(Integer id);

    Article selectArticleById(Integer id);
}
