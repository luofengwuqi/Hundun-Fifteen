package com.createpen.fifteenhundun.module.article.mapper;

import com.createpen.fifteenhundun.module.article.pojo.Entity.ArticleUserInteract;
import com.createpen.fifteenhundun.module.article.pojo.DTO.ArticleUpdateDTO;
import com.createpen.fifteenhundun.module.article.pojo.Entity.Article;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleMapper{

    List<Article> selectArticleList(@Param("userStatus") Integer userStatus, @Param("belonging") Integer belonging, @Param("articleStatus") Integer articleStatus);


    @Insert("INSERT INTO article (title, text, creator, belonging , created_at,user_id,status) VALUES (#{title}, #{text}, #{creator}, #{belonging},#{createdAt},#{userId},#{status})")
    void insertArticle(Article article);

    @Select("select * from article where id = #{id}")
    Article selectArticleById(Integer id);

    void batchUpdateArticleGoodCount(@Param("list") List<ArticleUpdateDTO> list);

    void batchUpdateArticleBadCount(List<ArticleUpdateDTO> articleUpdateDTOS);

    void batchInsertArticleUserInteract(List<ArticleUserInteract> goodUserRelationMap);

    @Delete("delete from article where id = #{id}")
    void deleteArticleById(Integer id);

    @Update("update article set status = #{status} where id = #{id}")
    void updateArticleStatus(@Param("id") Integer id, @Param("status") Integer status);

}
