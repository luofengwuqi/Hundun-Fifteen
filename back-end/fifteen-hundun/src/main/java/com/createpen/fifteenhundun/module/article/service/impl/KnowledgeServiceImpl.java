package com.createpen.fifteenhundun.module.article.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.module.article.mapper.KnowledgeMapper;
import com.createpen.fifteenhundun.module.article.pojo.DTO.CollectionDTO;
import com.createpen.fifteenhundun.module.article.pojo.Entity.Article;
import com.createpen.fifteenhundun.module.article.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {
    @Autowired
    private KnowledgeMapper knowledgeMapper;

    @Override
    public List<Article> selectCollection() {
        //TODO: 这里的代码有待完善
        Long userId = BaseContext.getCurrentId();
        return List.of();
    }

    @Override
    public List<Article> selectOwn() {
        Long userId = BaseContext.getCurrentId();

        return knowledgeMapper.selectOwn(userId);
    }

    @Override
    public void deleteCollection(CollectionDTO id) {
        //todo: 待实现
    }
}
