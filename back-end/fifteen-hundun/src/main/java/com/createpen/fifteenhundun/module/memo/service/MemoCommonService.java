package com.createpen.fifteenhundun.module.memo.service;

import com.createpen.fifteenhundun.module.memo.pojo.DTO.MemoCommonDTO;
import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoCommon;

import java.util.List;

public interface MemoCommonService {
    
    /**
     * 创建普通备忘
     */
    void insertMemoCommon(MemoCommonDTO dto);
    
    /**
     * 删除普通备忘
     */
    void deleteMemoCommonById(Long id);
    
    /**
     * 更新普通备忘
     */
    void updateMemoCommon(MemoCommonDTO dto);
    
    /**
     * 根据ID查询普通备忘
     */
    MemoCommon selectMemoCommonById(Long id);
    
    /**
     * 查询当前用户的所有普通备忘
     */
    List<MemoCommon> selectMemoCommonsByUserId(Long userId);
    
    /**
     * 查询所有普通备忘（管理员功能）
     */
    List<MemoCommon> selectAllMemoCommons();
}
