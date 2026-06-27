package com.createpen.fifteenhundun.module.article.service;

import com.createpen.fifteenhundun.module.article.pojo.DTO.CollectionDTO;
import com.createpen.fifteenhundun.module.article.pojo.Entity.Article;

import java.util.List;

public interface KnowledgeService {
    List<Article> selectCollection();

    List<Article> selectOwn();

    void deleteCollection(CollectionDTO id);
}
