package com.createpen.fifteenhundun.module.music.service;

import com.createpen.fifteenhundun.module.music.pojo.DTO.CreateMusicRoomDto;
import com.createpen.fifteenhundun.module.music.pojo.DTO.SongDTO;
import com.createpen.fifteenhundun.module.music.pojo.entity.MusicRoom;

public interface MusicRoomService {

    void createMusicRoom(CreateMusicRoomDto dto);

    void joinMusicRoom(CreateMusicRoomDto dto);

    MusicRoom initMusicRoomData();

    void exitMusicRoom();

    void addSong(SongDTO dto);

    void deleteSong(SongDTO dto);

    void switchSong(SongDTO dto);
}
