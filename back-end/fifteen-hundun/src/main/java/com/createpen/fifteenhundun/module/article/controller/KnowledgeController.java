package com.createpen.fifteenhundun.module.article.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.article.pojo.DTO.CollectionDTO;
import com.createpen.fifteenhundun.module.article.pojo.Entity.Article;
import com.createpen.fifteenhundun.module.article.service.KnowledgeService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    //取消收藏指定的文章
    @DeleteMapping("/collection")
    public Result<Void> deleteCollection(@RequestBody CollectionDTO dto){
        knowledgeService.deleteCollection(dto);
        return Result.success();
    }

    //查询自己的文章
    @GetMapping("/own")
    public Result<List<Article>> selectOwn(){
        List<Article> list = knowledgeService.selectOwn();
        return Result.success(list);
    }

    //分页查询自己的文章
    @GetMapping("/own/page")
    public Result<PageInfo<Article>> selectOwnPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Article> pageInfo = knowledgeService.selectOwnPage(page, pageSize);
        return Result.success(pageInfo);
    }

}
