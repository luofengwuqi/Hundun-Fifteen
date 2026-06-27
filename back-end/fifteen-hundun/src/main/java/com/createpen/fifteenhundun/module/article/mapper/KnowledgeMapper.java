package com.createpen.fifteenhundun.module.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.createpen.fifteenhundun.module.article.pojo.Entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgeMapper extends BaseMapper<Article> {
    @Select("select * from article where user_id = #{userId}")
    List<Article> selectOwn(Long userId);
}
