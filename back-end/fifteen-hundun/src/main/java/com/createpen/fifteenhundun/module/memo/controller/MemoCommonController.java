package com.createpen.fifteenhundun.module.memo.controller;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.memo.pojo.DTO.MemoCommonDTO;
import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoCommon;
import com.createpen.fifteenhundun.module.memo.service.MemoCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("memo/common")
@Slf4j
//日常的备忘记录 todolist
public class MemoCommonController {
    private final MemoCommonService memoCommonService;

    public MemoCommonController(MemoCommonService memoCommonService) {
        this.memoCommonService = memoCommonService;
    }

    //手动执行定时任务:发送邮件通知给开启邮件通知功能的用户
    @PostMapping("/email")
    public Result<Void> sendEmailToMemoUser() {
        log.info("手动执行定时任务: 发送邮件通知给开启邮件通知功能的用户");
        memoCommonService.sendEmailToMemoUser();
        return Result.success();
    }

    /**
     * 创建普通备忘
     */
    @PostMapping
    public Result<Void> createMemo(@RequestBody MemoCommonDTO dto) {
        log.info("创建普通备忘: {}", dto);
        memoCommonService.insertMemoCommon(dto);
        return Result.success();
    }
    
    /**
     * 删除普通备忘
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMemo(@PathVariable Long id) {
        log.info("删除普通备忘, id: {}", id);
        memoCommonService.deleteMemoCommonById(id);
        return Result.success();
    }
    
    /**
     * 更新普通备忘
     */
    @PutMapping("/{id}")
    public Result<Void> updateMemo(@PathVariable Long id, @RequestBody MemoCommonDTO dto) {
        log.info("更新普通备忘, id: {}", id);
        dto.setId(id);
        memoCommonService.updateMemoCommon(dto);
        return Result.success();
    }
    
    /**
     * 查询单个普通备忘
     */
    @GetMapping("/{id}")
    public Result<MemoCommon> getMemo(@PathVariable Long id) {
        log.info("查询普通备忘, id: {}", id);
        MemoCommon memo = memoCommonService.selectMemoCommonById(id);
        return Result.success(memo);
    }

    /**
     * 查询当前用户的普通备忘列表
     * @param completion 可选：pending-未完成，done-已完成；不传为全部
     */
    @GetMapping
    public Result<List<MemoCommon>> getUserMemos(
            @RequestParam(required = false) String completion) {
        log.info("查询当前用户的普通备忘列表, completion: {}", completion);
        Long userId = BaseContext.getCurrentId();
        String filter = normalizeCompletion(completion);
        List<MemoCommon> memos = memoCommonService.selectMemoCommonsByUserId(userId, filter);
        return Result.success(memos);
    }

    private static String normalizeCompletion(String completion) {
        if (completion == null || completion.isBlank()) {
            return null;
        }
        return switch (completion.trim().toLowerCase()) {
            case "pending", "done" -> completion.trim().toLowerCase();
            default -> null;
        };
    }
}
