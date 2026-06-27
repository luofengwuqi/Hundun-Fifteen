package com.createpen.fifteenhundun.module.article.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.article.pojo.Entity.Article;
import com.createpen.fifteenhundun.module.article.service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {
    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    //查询收藏的文章
    @GetMapping("/collection")
    public Result<List<Article>> selectCollection(){
        List<Article> list = knowledgeService.selectCollection();
        return Result.success(list);
    }

    //查询自己的文章
    @GetMapping("/own")
    public Result<List<Article>> selectOwn(){
        List<Article> list = knowledgeService.selectOwn();
        return Result.success(list);
    }
}
