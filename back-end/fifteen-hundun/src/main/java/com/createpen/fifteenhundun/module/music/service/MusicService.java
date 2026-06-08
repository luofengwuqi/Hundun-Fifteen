package com.createpen.fifteenhundun.module.music.service;

import com.createpen.fifteenhundun.module.music.pojo.DTO.MusicDTO;
import com.createpen.fifteenhundun.module.music.pojo.entity.Music;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MusicService {

    void insertMusic(MusicDTO dto);

    void deleteMusicById(Long id);

    void updateMusic(MusicDTO dto);

    Music selectMusicById(Long id);

    List<Music> selectAllMusic();

    String uploadAudio(MultipartFile file);

    String uploadCover(MultipartFile file);
}
