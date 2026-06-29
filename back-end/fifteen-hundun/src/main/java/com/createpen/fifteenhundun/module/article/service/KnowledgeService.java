package com.createpen.fifteenhundun.module.article.service;

import com.createpen.fifteenhundun.module.article.pojo.DTO.CollectionDTO;
import com.createpen.fifteenhundun.module.article.pojo.Entity.Article;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface KnowledgeService {
    List<Article> selectCollection();

    List<Article> selectOwn();

    PageInfo<Article> selectOwnPage(Integer page, Integer pageSize);

    void deleteCollection(CollectionDTO id);
}
