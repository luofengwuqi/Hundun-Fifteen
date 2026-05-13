package com.createpen.fifteenhundun.module.memo.service;

import com.createpen.fifteenhundun.module.memo.pojo.DTO.MemoImportantDTO;
import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoImportant;

import java.util.List;

public interface MemoImportantService {
    
    /**
     * 创建重要备忘
     */
    void insertMemoImportant(MemoImportantDTO dto);
    
    /**
     * 删除重要备忘
     */
    void deleteMemoImportantById(Long id);
    
    /**
     * 更新重要备忘
     */
    void updateMemoImportant(MemoImportantDTO dto);
    
    /**
     * 根据ID查询重要备忘
     */
    MemoImportant selectMemoImportantById(Long id);
    
    /**
     * 查询当前用户的所有重要备忘
     */
    List<MemoImportant> selectMemoImportantsByUserId(Long userId);
    
    /**
     * 查询所有重要备忘（管理员功能）
     */
    List<MemoImportant> selectAllMemoImportants();
}
