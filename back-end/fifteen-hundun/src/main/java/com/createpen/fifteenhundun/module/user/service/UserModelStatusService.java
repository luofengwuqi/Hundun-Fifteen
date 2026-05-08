package com.createpen.fifteenhundun.module.user.service;

import com.createpen.fifteenhundun.module.user.pojo.DTO.UpdateUserModelStatusDTO;
import com.createpen.fifteenhundun.module.user.pojo.Entity.UserModelStatus;

public interface UserModelStatusService {

    /**
     * 获取用户模块状态
     */
    UserModelStatus getUserModelStatus(Long userId);

    /**
     * 初始化用户模块状态（新用户注册时调用）
     */
    void initUserModelStatus(Long userId);

    /**
     * 更新用户模块状态
     */
    void updateUserModelStatus(UpdateUserModelStatusDTO userModelStatus);

    /**
     * 删除用户模块状态
     */
    void deleteUserModelStatus(Long userId);
}
