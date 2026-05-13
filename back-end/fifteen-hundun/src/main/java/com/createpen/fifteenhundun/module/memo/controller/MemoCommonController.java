package com.createpen.fifteenhundun.module.memo.controller;

import com.createpen.fifteenhundun.module.memo.service.MemoCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("memo/common")
@Slf4j
//日常的备忘记录 todolist
public class MemoCommonController {
    private final MemoCommonService memoCommonService;

    public MemoCommonController(MemoCommonService memoCommonService) {
        this.memoCommonService = memoCommonService;
    }
}
