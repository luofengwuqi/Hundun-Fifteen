package com.createpen.fifteenhundun.module.music.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.exception.MyException.PasswordException;
import com.createpen.fifteenhundun.common.exception.MyException.UserExistedException;
import com.createpen.fifteenhundun.module.music.cache.MusicRoomCache;
import com.createpen.fifteenhundun.module.music.pojo.DTO.CreateMusicRoomDto;
import com.createpen.fifteenhundun.module.music.pojo.DTO.SongDTO;
import com.createpen.fifteenhundun.module.music.pojo.entity.MusicRoom;
import com.createpen.fifteenhundun.module.music.service.MusicRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class MusicRoomServiceImpl implements MusicRoomService {
    private final MusicRoomCache musicRoomCache;

    public MusicRoomServiceImpl(MusicRoomCache musicRoomCache) {
        this.musicRoomCache = musicRoomCache;
    }


    @Override
    public void createMusicRoom(CreateMusicRoomDto dto) {
        Long currentId = BaseContext.getCurrentId();
        //校验房间是否已经存在
        if(musicRoomCache.isRoomExist(currentId)){
            log.warn("userId:{} 已创建房间", currentId);
//            throw new UserExistedException("已经有房间了哦...");
            return;
        }
        musicRoomCache.createRoom(currentId);
        musicRoomCache.setUserInRoom(currentId,currentId);
        musicRoomCache.setRoomPassword(dto.getRoomSecret(),currentId);
    }

    @Override
    public void joinMusicRoom(CreateMusicRoomDto dto) {
        Long ownerId = musicRoomCache.getOwnerIdByPassword(dto.getRoomSecret());
        if(ownerId == null){
            log.info("密码不存在");
            throw new PasswordException("密码输错了或房间不存在哦...");
        }
        //校验通过
        musicRoomCache.setUserInRoom(BaseContext.getCurrentId(),ownerId);
    }

    @Override
    public MusicRoom initMusicRoomData() {
        Long userCurrentRoom = musicRoomCache.getUserCurrentRoom(BaseContext.getCurrentId());
        if(userCurrentRoom == null){
            log.error("您还没有创建/加入房间哦...");
            throw new RuntimeException("您还没有创建/加入房间哦...");
        }
        return new MusicRoom(userCurrentRoom,
                musicRoomCache.getPlaylist(userCurrentRoom),
                musicRoomCache.getCurrentMusic(userCurrentRoom),
                null);
    }

    @Override
    public void exitMusicRoom() {
        Long currentUserId = BaseContext.getCurrentId();
        Long ownerId = musicRoomCache.getUserCurrentRoom(currentUserId);
        //本人退出
        if(Objects.equals(currentUserId, ownerId)){
            log.info("用户:{} 退出房间{}", currentUserId,ownerId);
            musicRoomCache.cleanupRoom(currentUserId);
            //todo:websocket发送给前端 踢出所有人?)
            return;
        }
        //非本人退出
        musicRoomCache.userLeaveRoom(currentUserId);
    }

    @Override
    public void addSong(SongDTO dto) {
        log.info("用户:{} 加入歌曲:{}", BaseContext.getCurrentId(),dto.getSongId());
        musicRoomCache.addToPlaylist(BaseContext.getCurrentId(),dto.getSongId());
    }

    @Override
    public void deleteSong(SongDTO dto) {
        log.info("用户:{} 删除歌曲:{}", BaseContext.getCurrentId(),dto.getSongId());
        musicRoomCache.removeFromPlaylist(BaseContext.getCurrentId(),dto.getSongId());
    }

    @Override
    public void switchSong(SongDTO dto) {
        log.info("用户:{} 切换歌曲:{}", BaseContext.getCurrentId(),dto.getSongId());
        musicRoomCache.setCurrentMusic(BaseContext.getCurrentId(),dto.getSongId());
    }
}
