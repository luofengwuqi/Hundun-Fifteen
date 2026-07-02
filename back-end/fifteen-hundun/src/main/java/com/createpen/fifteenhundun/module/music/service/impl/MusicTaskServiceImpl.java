package com.createpen.fifteenhundun.module.music.service.impl;

import com.createpen.fifteenhundun.common.util.AliOSSUtil;
import com.createpen.fifteenhundun.module.music.mapper.MusicMapper;
import com.createpen.fifteenhundun.module.music.pojo.entity.Music;
import com.createpen.fifteenhundun.module.music.service.MusicTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.createpen.fifteenhundun.common.constant.oss.MusicOssConstant.MUSIC_AUDIO_FOLDER;
import static com.createpen.fifteenhundun.common.constant.oss.MusicOssConstant.MUSIC_COVER_FOLDER;

@Slf4j
@Service
public class MusicTaskServiceImpl implements MusicTaskService {

    private final AliOSSUtil aliOssUtil;
    private final MusicMapper musicMapper;

    public MusicTaskServiceImpl(AliOSSUtil aliOssUtil, MusicMapper musicMapper) {
        this.aliOssUtil = aliOssUtil;
        this.musicMapper = musicMapper;
    }

    @Override
    @Transactional
    public void ossToMysql() {
        log.info("========== 开始 OSS → MySQL 同步任务 ==========");

        // 1. 获取MySQL中已有的音乐记录（用于去重）
        List<Music> existingMusicList = musicMapper.selectAllMusic();
        Set<String> existingAudioUrls = existingMusicList.stream()
                .map(Music::getAudioUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        log.info("MySQL现有 {} 条音乐记录", existingMusicList.size());

        // 2. 从OSS列出所有音频文件
        List<String> audioObjects = aliOssUtil.listObjects(MUSIC_AUDIO_FOLDER);
        // 过滤掉目录占位符（OSS模拟目录时会产生空对象）
        audioObjects = audioObjects.stream()
                .filter(obj -> !obj.endsWith("/"))
                .collect(Collectors.toList());
        log.info("OSS中有效音频文件数量: {}", audioObjects.size());

        // 3. 从OSS列出所有封面文件，构建 baseName → coverUrl 的映射
        List<String> coverObjects = aliOssUtil.listObjects(MUSIC_COVER_FOLDER);
        coverObjects = coverObjects.stream()
                .filter(obj -> !obj.endsWith("/"))
                .collect(Collectors.toList());
        log.info("OSS中有效封面文件数量: {}", coverObjects.size());

        String domain = aliOssUtil.getDomain();
        Map<String, String> coverBaseNameMap = new HashMap<>();
        for (String coverObj : coverObjects) {
            String fileName = coverObj.substring(coverObj.lastIndexOf("/") + 1);
            String baseName = fileName.contains(".") ?
                    fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
            coverBaseNameMap.put(baseName, domain + "/" + coverObj);
        }

        // 4. 遍历音频文件，补全MySQL缺失的记录
        int skipCount = 0;
        int insertCount = 0;
        int coverMatchCount = 0;
        List<String> addedTitles = new ArrayList<>();

        for (String audioObject : audioObjects) {
            String audioUrl = domain + "/" + audioObject;

            // MySQL中已存在则跳过
            if (existingAudioUrls.contains(audioUrl)) {
                skipCount++;
                continue;
            }

            // 从文件名推导标题（不带扩展名）
            String fileName = audioObject.substring(audioObject.lastIndexOf("/") + 1);
            String title = fileName.contains(".") ?
                    fileName.substring(0, fileName.lastIndexOf(".")) : fileName;

            // 尝试按同名 baseName 匹配封面
            String coverUrl = coverBaseNameMap.get(title);
            if (coverUrl != null) {
                coverMatchCount++;
            }

            // 插入新音乐记录
            Music music = new Music();
            music.setTitle(title);
            music.setDuration(0);
            music.setAudioUrl(audioUrl);
            music.setCoverUrl(coverUrl);
            music.setUploaderName("oss-sync");

            musicMapper.insertMusic(music);
            insertCount++;
            addedTitles.add(title);
            log.debug("新增音乐: title={}, audioUrl={}, coverUrl={}",
                    title, audioUrl, coverUrl != null ? coverUrl : "无");
        }

        // 5. 找出OSS中有封面但无对应音频的孤立文件（仅做日志警告）
        Set<String> audioBaseNames = audioObjects.stream()
                .map(obj -> {
                    String fn = obj.substring(obj.lastIndexOf("/") + 1);
                    return fn.contains(".") ? fn.substring(0, fn.lastIndexOf(".")) : fn;
                })
                .collect(Collectors.toSet());

        List<String> orphanCovers = coverObjects.stream()
                .filter(obj -> {
                    String fn = obj.substring(obj.lastIndexOf("/") + 1);
                    String bn = fn.contains(".") ? fn.substring(0, fn.lastIndexOf(".")) : fn;
                    return !audioBaseNames.contains(bn);
                })
                .collect(Collectors.toList());

        if (!orphanCovers.isEmpty()) {
            log.warn("发现 {} 个孤立封面文件（无对应音频）: {}", orphanCovers.size(), orphanCovers);
        }

        // 6. 汇总日志
        log.info("========== OSS → MySQL 同步完成 ==========");
        log.info("已存在(跳过): {} 条", skipCount);
        log.info("新增插入: {} 条", insertCount);
        log.info("封面自动关联: {} 条", coverMatchCount);
        if (!addedTitles.isEmpty()) {
            log.info("新增歌曲标题: {}", addedTitles);
        }
    }
}
