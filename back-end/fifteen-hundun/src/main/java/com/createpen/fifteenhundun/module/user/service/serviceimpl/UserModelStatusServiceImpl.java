package com.createpen.fifteenhundun.module.user.service.serviceimpl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.module.user.cache.UserModelStatusRedisService;
import com.createpen.fifteenhundun.module.user.mapper.UserModelStatusMapper;
import com.createpen.fifteenhundun.module.user.pojo.DTO.UpdateUserModelStatusDTO;
import com.createpen.fifteenhundun.module.user.pojo.Entity.UserModelStatus;
import com.createpen.fifteenhundun.module.user.service.UserModelStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserModelStatusServiceImpl implements UserModelStatusService {

    private final UserModelStatusRedisService umsRedisService;
    private final UserModelStatusMapper userModelStatusMapper;

    public UserModelStatusServiceImpl(UserModelStatusMapper userModelStatusMapper, UserModelStatusRedisService umsRedisService) {
        this.userModelStatusMapper = userModelStatusMapper;
        this.umsRedisService = umsRedisService;
    }

    @Override
    public UserModelStatus getUserModelStatus(Long userId) {
        log.info("获取用户模块状态, userId: {}", userId);
        // 先从缓存中获取
        UserModelStatus status = umsRedisService.selectUMSinRedis(userId);
        if (status != null) {
            log.info("缓存命中");
            return status;
        }

        log.info("缓存未命中，查询数据库");
        status = userModelStatusMapper.selectByUserId(userId);

        // 如果数据库中有数据，写入缓存后返回
        if (status != null) {
            log.info("数据库命中，写入缓存");
            umsRedisService.insertUMSinRedis(userId, status);
            return status;
        }

        // 如果数据库中也没有，初始化默认状态
        log.info("用户模块状态不存在，初始化默认状态, userId: {}", userId);
        status = initUserModelStatus(userId);
        //这里可能会存在多线程问题 解决方法:在数据库里面加了唯一约束 如果多次插入那么捕获sql异常
        try{
            userModelStatusMapper.insertUserModelStatus(status);
        }catch(Exception e){
            log.error("插入用户模块状态失败:{}",e.getMessage());
        }
        // 将初始化后的数据写入缓存
        umsRedisService.insertUMSinRedis(userId, status);
        return status;
    }

    @Override
    public UserModelStatus initUserModelStatus(Long userId) {
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
        return status;
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

        // 移除缓存
        umsRedisService.deleteUMSinRedis(dto.getUserId());
    }

    @Override
    public void deleteUserModelStatus(Long userId) {
        log.info("删除用户模块状态, userId: {}", userId);
        userModelStatusMapper.deleteUserModelStatusByUserId(userId);
        umsRedisService.deleteUMSinRedis(userId);
    }
}
