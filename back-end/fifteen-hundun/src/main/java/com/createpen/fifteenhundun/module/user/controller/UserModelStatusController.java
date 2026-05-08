package com.createpen.fifteenhundun.module.user.controller;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.user.pojo.DTO.UpdateUserModelStatusDTO;
import com.createpen.fifteenhundun.module.user.pojo.Entity.UserModelStatus;
import com.createpen.fifteenhundun.module.user.service.UserModelStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/model-status")
@Slf4j
public class UserModelStatusController {

    private final UserModelStatusService userModelStatusService;

    public UserModelStatusController(UserModelStatusService userModelStatusService) {
        this.userModelStatusService = userModelStatusService;
    }

    /**
     * 获取当前用户的模块状态
     */
    @GetMapping
    public Result<UserModelStatus> getUserModelStatus() {
        log.info("获取用户模块状态");
        Long userId = BaseContext.getCurrentId();
        UserModelStatus status = userModelStatusService.getUserModelStatus(userId);
        return Result.success(status);
    }

    /**
     * 更新用户模块状态
     */
    @PutMapping
    public Result<Void> updateUserModelStatus(@RequestBody UpdateUserModelStatusDTO dto) {
        log.info("更新用户模块状态: {}", dto);
        userModelStatusService.updateUserModelStatus(dto);
        return Result.success();
    }

    //警告这个方法有一定的危险性
    @DeleteMapping
    public Result<Void> deleteUserModelStatus(@RequestBody UserModelStatus userModelStatus) {
        log.info("删除用户模块状态");
        userModelStatusService.deleteUserModelStatus(userModelStatus.getUserId());
        return Result.success();
    }
}
