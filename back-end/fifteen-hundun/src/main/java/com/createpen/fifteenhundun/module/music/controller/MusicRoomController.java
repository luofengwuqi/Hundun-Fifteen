package com.createpen.fifteenhundun.module.music.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.music.pojo.DTO.CreateMusicRoomDto;
import com.createpen.fifteenhundun.module.music.pojo.DTO.SongDTO;
import com.createpen.fifteenhundun.module.music.pojo.entity.MusicRoom;
import com.createpen.fifteenhundun.module.music.service.MusicRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/musicRoom")
@Slf4j
public class MusicRoomController {

    private final MusicRoomService musicRoomService;

    public MusicRoomController(MusicRoomService musicRoomService) {
        this.musicRoomService = musicRoomService;
    }
    //加载页面 调用这个
    @GetMapping
    public Result<MusicRoom> initMusicRoomData() {
        log.info("初始化音乐房间数据");
        MusicRoom res =  musicRoomService.initMusicRoomData();
        return Result.success(res);
    }

    //新建音乐房间
    @PostMapping
    public Result<Void> createMusicRoom(@RequestBody CreateMusicRoomDto dto) {
        log.info("新建音乐房间");
        musicRoomService.createMusicRoom(dto);
        return Result.success();
    }

    //加入音乐房间
    @PostMapping("/join")
    public Result<Void> joinMusicRoom(@RequestBody CreateMusicRoomDto dto) {
        log.info("加入音乐房间");
        musicRoomService.joinMusicRoom(dto);
        return Result.success();
    }
    //退出音乐房间
    @DeleteMapping
    public Result<Void> exitMusicRoom() {
        log.info("退出音乐房间");
        musicRoomService.exitMusicRoom();
        return Result.success();
    }

    //加歌
    @PostMapping("/song")
    public Result<Void> addSong(@RequestBody SongDTO dto) {
        log.info("加歌");
        musicRoomService.addSong(dto);
        return Result.success();
    }
    //删歌
    @DeleteMapping("/song")
    public Result<Void> deleteSong(@RequestBody SongDTO dto) {
        log.info("删歌");
        musicRoomService.deleteSong(dto);
        return Result.success();
    }

    //切歌
    @PostMapping("/song/switch")
    public Result<Void> switchSong(@RequestBody SongDTO dto) {
        log.info("切歌");
        musicRoomService.switchSong(dto);
        return Result.success();
    }

    //获取

}
