package com.createpen.fifteenhundun.module.memo.controller;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.memo.pojo.DTO.MemoImportantDTO;
import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoImportant;
import com.createpen.fifteenhundun.module.memo.service.MemoImportantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("memo/important")
@Slf4j
//重要的记录
public class MemoImportantController {
    private final MemoImportantService memoImportantService;

    public MemoImportantController(MemoImportantService memoImportantService) {
        this.memoImportantService = memoImportantService;
    }
    
    /**
     * 创建重要备忘
     */
    @PostMapping
    public Result<Void> createMemo(@RequestBody MemoImportantDTO dto) {
        log.info("创建重要备忘: {}", dto);
        memoImportantService.insertMemoImportant(dto);
        return Result.success();
    }
    
    /**
     * 删除重要备忘
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMemo(@PathVariable Long id) {
        log.info("删除重要备忘, id: {}", id);
        memoImportantService.deleteMemoImportantById(id);
        return Result.success();
    }
    
    /**
     * 更新重要备忘
     */
    @PutMapping("/{id}")
    public Result<Void> updateMemo(@PathVariable Long id, @RequestBody MemoImportantDTO dto) {
        log.info("更新重要备忘, id: {}", id);
        dto.setId(id);
        memoImportantService.updateMemoImportant(dto);
        return Result.success();
    }
    
    /**
     * 查询单个重要备忘
     */
    @GetMapping("/{id}")
    public Result<MemoImportant> getMemo(@PathVariable Long id) {
        log.info("查询重要备忘, id: {}", id);
        MemoImportant memo = memoImportantService.selectMemoImportantById(id);
        return Result.success(memo);
    }
    
    /**
     * 查询当前用户的所有重要备忘
     */
    @GetMapping
    public Result<List<MemoImportant>> getUserMemos() {
        log.info("查询当前用户的重要备忘列表");
        Long userId = BaseContext.getCurrentId();
        List<MemoImportant> memos = memoImportantService.selectMemoImportantsByUserId(userId);
        return Result.success(memos);
    }
}
