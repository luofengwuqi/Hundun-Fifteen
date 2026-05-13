package com.createpen.fifteenhundun.module.memo.controller;

import com.createpen.fifteenhundun.module.memo.service.MemoImportantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("memo/important")
@Slf4j
//重要的记录
public class MemoImportantController {
    private final MemoImportantService memoImportantService;

    public MemoImportantController(MemoImportantService memoImportantService) {
        this.memoImportantService = memoImportantService;
    }
}
