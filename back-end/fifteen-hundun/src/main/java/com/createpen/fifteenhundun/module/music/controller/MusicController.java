package com.createpen.fifteenhundun.module.music.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.music.pojo.DTO.MusicDTO;
import com.createpen.fifteenhundun.module.music.pojo.entity.Music;
import com.createpen.fifteenhundun.module.music.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/music")
public class MusicController {
    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @PostMapping
    public Result<Void> insertMusic(@RequestBody MusicDTO dto) {
        log.info("添加音乐");
        musicService.insertMusic(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMusic(@PathVariable Long id) {
        log.info("删除音乐, id={}", id);
        musicService.deleteMusicById(id);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateMusic(@PathVariable Long id, @RequestBody MusicDTO dto) {
        log.info("更新音乐, id={}", id);
        dto.setId(id);
        musicService.updateMusic(dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Music> selectMusicById(@PathVariable Long id) {
        log.info("获取音乐, id={}", id);
        Music music = musicService.selectMusicById(id);
        return Result.success(music);
    }

    @GetMapping
    public Result<List<Music>> selectAllMusic() {
        log.info("获取所有音乐");
        List<Music> musicList = musicService.selectAllMusic();
        return Result.success(musicList);
    }

    @PostMapping("/upload")
    public Result<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        log.info("上传音频文件");
        String url = musicService.uploadAudio(file);
        return Result.success("上传成功", url);
    }

    @PostMapping("/upload/cover")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) {
        log.info("上传封面图片");
        String url = musicService.uploadCover(file);
        return Result.success("上传成功", url);
    }
}
