package com.createpen.fifteenhundun.module.music.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.music.service.MusicTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
@Slf4j
public class MusicTaskController {

    private final MusicTaskService musicTaskService;

    public MusicTaskController(MusicTaskService musicTaskService) {
        this.musicTaskService = musicTaskService;
    }

    //    主动从OSS获取信息到数据库
    @PostMapping("/music/task/oss")
    public Result<Void> ossToMysql(){
        log.info("从OSS获取信息到数据库");
        musicTaskService.ossToMysql();
        return Result.success();
    }
}
