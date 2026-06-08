package com.createpen.fifteenhundun.module.music.mapper;

import com.createpen.fifteenhundun.module.music.pojo.entity.Music;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MusicMapper {

    void insertMusic(Music music);

    void deleteMusicById(Long id);

    void updateMusic(Music music);

    Music selectMusicById(Long id);

    List<Music> selectAllMusic();
}
