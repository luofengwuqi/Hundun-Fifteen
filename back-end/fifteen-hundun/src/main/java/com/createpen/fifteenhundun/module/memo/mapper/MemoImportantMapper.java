package com.createpen.fifteenhundun.module.memo.mapper;

import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoImportant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemoImportantMapper {
    
    /**
     * 插入重要备忘
     */
    void insertMemoImportant(MemoImportant memoImportant);
    
    /**
     * 根据ID删除重要备忘
     */
    void deleteMemoImportantById(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 更新重要备忘
     */
    void updateMemoImportant(MemoImportant memoImportant);
    
    /**
     * 根据ID查询重要备忘
     */
    MemoImportant selectMemoImportantById(Long id);
    
    /**
     * 根据用户ID查询所有重要备忘
     */
    List<MemoImportant> selectMemoImportantsByUserId(Long userId);
    
    /**
     * 查询所有重要备忘
     */
    List<MemoImportant> selectAllMemoImportants();
}
