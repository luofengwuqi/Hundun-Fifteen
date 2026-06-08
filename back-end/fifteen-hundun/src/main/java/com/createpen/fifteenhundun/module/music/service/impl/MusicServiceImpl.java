package com.createpen.fifteenhundun.module.music.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.util.AliOSSUtil;
import com.createpen.fifteenhundun.module.music.mapper.MusicMapper;
import com.createpen.fifteenhundun.module.music.pojo.DTO.MusicDTO;
import com.createpen.fifteenhundun.module.music.pojo.entity.Music;
import com.createpen.fifteenhundun.module.music.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.createpen.fifteenhundun.common.constant.oss.MusicOssConstant.MUSIC_AUDIO_FOLDER;
import static com.createpen.fifteenhundun.common.constant.oss.MusicOssConstant.MUSIC_COVER_FOLDER;

@Slf4j
@Service
public class MusicServiceImpl implements MusicService {

    private final MusicMapper musicMapper;
    private final AliOSSUtil aliOssUtil;

    public MusicServiceImpl(MusicMapper musicMapper, AliOSSUtil aliOssUtil) {
        this.musicMapper = musicMapper;
        this.aliOssUtil = aliOssUtil;
    }

    @Transactional
    @Override
    public void insertMusic(MusicDTO dto) {
        Music music = new Music();
        BeanUtils.copyProperties(dto, music);
        if (dto.getUploaderName() == null || dto.getUploaderName().isBlank()) {
            music.setUploaderName(BaseContext.getCurrentUsername());
        }
        musicMapper.insertMusic(music);
        log.info("音乐已添加, id={}", music.getId());
    }

    @Transactional
    @Override
    public void deleteMusicById(Long id) {
        Music music = musicMapper.selectMusicById(id);
        if (music != null && music.getAudioUrl() != null) {
            aliOssUtil.delete(music.getAudioUrl());
        }
        musicMapper.deleteMusicById(id);
        log.info("音乐已删除, id={}", id);
    }

    @Transactional
    @Override
    public void updateMusic(MusicDTO dto) {
        Music music = new Music();
        BeanUtils.copyProperties(dto, music);
        musicMapper.updateMusic(music);
        log.info("音乐已更新, id={}", music.getId());
    }

    @Override
    public Music selectMusicById(Long id) {
        return musicMapper.selectMusicById(id);
    }

    @Override
    public List<Music> selectAllMusic() {
        return musicMapper.selectAllMusic();
    }

    @Override
    public String uploadCover(MultipartFile file) {
        String fileName = aliOssUtil.makeUUIDFileName(file);
        String objectName = MUSIC_COVER_FOLDER + fileName;
        try {
            String url = aliOssUtil.upload(file, objectName);
            log.info("封面上传成功, url={}", url);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("封面上传失败", e);
        }
    }

    @Override
    public String uploadAudio(MultipartFile file) {
        String fileName = aliOssUtil.makeUUIDFileName(file);
        String objectName = MUSIC_AUDIO_FOLDER + fileName;
        try {
            String url = aliOssUtil.upload(file, objectName);
            log.info("音频上传成功, url={}", url);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("音频上传失败", e);
        }
    }
}
