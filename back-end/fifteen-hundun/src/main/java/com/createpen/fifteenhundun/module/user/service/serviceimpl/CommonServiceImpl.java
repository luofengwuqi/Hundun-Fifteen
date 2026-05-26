package com.createpen.fifteenhundun.module.user.service.serviceimpl;

import com.createpen.fifteenhundun.common.config.UserRelatedTablesConfig;
import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.util.AliOSSUtil;
import com.createpen.fifteenhundun.module.user.mapper.CommonMapper;
import com.createpen.fifteenhundun.module.user.pojo.Entity.User;
import com.createpen.fifteenhundun.module.user.service.CommonService;
import com.createpen.fifteenhundun.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class CommonServiceImpl implements CommonService {

    private final UserService userService;
    private final CommonMapper commonMapper;
    private final UserRelatedTablesConfig userRelatedTablesConfig;
    private final AliOSSUtil aliOssUtil;
    
    public CommonServiceImpl(UserRelatedTablesConfig userRelatedTablesConfig, AliOSSUtil aliOssUtil, CommonMapper commonMapper, UserService userService) {
        this.userRelatedTablesConfig = userRelatedTablesConfig;
        this.aliOssUtil = aliOssUtil;
        this.commonMapper = commonMapper;
        this.userService = userService;
    }
    private String makeFilePath(String username){
        // OSS存储路径: username/avatars/
        return username;
    }
    @Override
    public void deleteFile(Long userId) {
        User user = userService.selectUserById(userId);
        if (user == null) {
            log.warn("用户 {} 不存在，跳过文件删除", userId);
            return;
        }

        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            log.info("用户 {} 无头像，跳过文件删除", userId);
            return;
        }

        log.info("========== 开始删除用户 {} 的头像 ==========", userId);
        log.info("头像路径: {}", avatarUrl);

        try {
            aliOssUtil.deleteDirectory(user.getUsername()+"/");
            log.info("========== 用户 {} 的头像删除完成 ==========", userId);
        } catch (Exception e) {
            log.error("========== 用户 {} 的头像删除失败 ==========", userId, e);
            throw e;
        }
    }
    @Override
    public String addFile(MultipartFile file) {
        try {
            String username = BaseContext.getCurrentUsername();
            log.info("当前用户名：{}", username);
            // 生成唯一文件名
            String fileName = aliOssUtil.makeUUIDFileName(file);
            // 完整路径: username/avatars/filename
            String objectName = makeFilePath(username) + fileName;
            log.info("文件上传路径: {}", objectName);
            return aliOssUtil.upload(file, objectName);

        } catch (Exception e) {
            log.error("文件上传失败", e);
            return null;
        }
    }


    @Override
    @Transactional
    public void deleteRelatedTables(Long userId) {
        List<String> relatedTables = userRelatedTablesConfig.getRelatedTables();
        if (relatedTables == null || relatedTables.isEmpty()) {
            throw new RuntimeException("请配置用户相关表");
        }

        log.info("开始删除用户 {} 的相关表数据，共 {} 个表", userId, relatedTables.size());
        
        for(String tableName : relatedTables){
            try {
                commonMapper.deleteRelatedTables(userId, tableName);
                log.info("成功删除用户 {} 的表 {}", userId, tableName);
            } catch (Exception e) {
                log.error("删除用户 {} 的表 {} 失败，将回滚事务", userId, tableName, e);
                throw new RuntimeException("删除表 " + tableName + " 失败: " + e.getMessage(), e);
            }
        }
        
        log.info("用户 {} 的所有相关表删除完成", userId);
    }
    }
