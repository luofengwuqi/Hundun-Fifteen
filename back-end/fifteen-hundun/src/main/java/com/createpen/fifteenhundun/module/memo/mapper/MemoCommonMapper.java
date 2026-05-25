package com.createpen.fifteenhundun.module.memo.mapper;

import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoCommon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemoCommonMapper {
    
    /**
     * 插入普通备忘
     */
    void insertMemoCommon(MemoCommon memoCommon);
    
    /**
     * 根据ID删除普通备忘
     */
    void deleteMemoCommonById(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 更新普通备忘
     */
    void updateMemoCommon(MemoCommon memoCommon);
    
    /**
     * 根据ID查询普通备忘
     */
    MemoCommon selectMemoCommonById(Long id);
    
    /**
     * 根据用户ID查询普通备忘
     * @param completion 可选：pending-未完成，done-已完成；null 或空为全部
     */
    List<MemoCommon> selectMemoCommonsByUserId(
            @Param("userId") Long userId,
            @Param("completion") String completion);
    
    /**
     * 查询所有普通备忘
     */
    List<MemoCommon> selectAllMemoCommons();
}
