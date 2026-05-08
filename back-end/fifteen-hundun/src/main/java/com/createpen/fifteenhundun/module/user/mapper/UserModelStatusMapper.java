package com.createpen.fifteenhundun.module.user.mapper;

import com.createpen.fifteenhundun.module.user.pojo.Entity.UserModelStatus;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserModelStatusMapper {

    /**
     * 根据用户ID查询模块状态
     */
    @Select("SELECT * FROM user_model_status WHERE user_id = #{userId}")
    UserModelStatus selectByUserId(Long userId);

    /**
     * 插入用户模块状态
     */
    @Insert("INSERT INTO user_model_status (user_id, health, finance, knowledge, memo, is_new_user) VALUES (#{userId}, #{health}, #{finance}, #{knowledge}, #{memo}, #{isNewUser})")
    void insertUserModelStatus(UserModelStatus userModelStatus);

     /**
     * 更新用户模块状态
     */
    void updateUserModelStatus(UserModelStatus userModelStatus);

    /**
     * 删除用户模块状态
     */
    @Delete("DELETE FROM user_model_status WHERE user_id = #{userId}")
    void deleteUserModelStatusByUserId(Long userId);
}