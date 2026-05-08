package com.createpen.fifteenhundun.module.user.service.serviceimpl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.module.user.mapper.UserModelStatusMapper;
import com.createpen.fifteenhundun.module.user.pojo.DTO.UpdateUserModelStatusDTO;
import com.createpen.fifteenhundun.module.user.pojo.Entity.UserModelStatus;
import com.createpen.fifteenhundun.module.user.service.UserModelStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserModelStatusServiceImpl implements UserModelStatusService {

    @Autowired
    private UserModelStatusMapper userModelStatusMapper;

    @Override
    public UserModelStatus getUserModelStatus(Long userId) {
        log.info("获取用户模块状态, userId: {}", userId);
        UserModelStatus status = userModelStatusMapper.selectByUserId(userId);
        
        // 如果用户还没有模块状态记录，则初始化一个默认状态
        if (status == null) {
            log.info("用户模块状态不存在，初始化默认状态, userId: {}", userId);
            initUserModelStatus(userId);
            status = userModelStatusMapper.selectByUserId(userId);
        }
        
        return status;
    }

    @Override
    public void initUserModelStatus(Long userId) {
        log.info("初始化用户模块状态, userId: {}", userId);
        
        UserModelStatus status = new UserModelStatus();
        status.setUserId(userId);
        // 默认所有模块都禁用
        status.setHealth(0);
        status.setFinance(0);
        status.setKnowledge(0);
        status.setMemo(0);
        // 标记为新用户
        status.setIsNewUser(1);
        
        userModelStatusMapper.insertUserModelStatus(status);
    }

    @Override
    public void updateUserModelStatus(UpdateUserModelStatusDTO dto) {
        //先检验是否有userId传参 如果没有现塞一个当前登录用户的ID
        if (dto.getUserId() == null) {
            dto.setUserId(BaseContext.getCurrentId());
        }
        log.info("更新用户模块状态, userId: {}", dto.getUserId());
        UserModelStatus status = new UserModelStatus();
        BeanUtils.copyProperties(dto, status);
        // 先检查是否存在记录
        UserModelStatus existingStatus = userModelStatusMapper.selectByUserId(dto.getUserId());
        if (existingStatus == null) {
            // 如果不存在则插入新记录
            userModelStatusMapper.insertUserModelStatus(status);
        } else {
            // 如果存在则更新记录
            userModelStatusMapper.updateUserModelStatus(status);
        }
    }

    @Override
    public void deleteUserModelStatus(Long userId) {
        log.info("删除用户模块状态, userId: {}", userId);
        userModelStatusMapper.deleteUserModelStatusByUserId(userId);
    }
}
