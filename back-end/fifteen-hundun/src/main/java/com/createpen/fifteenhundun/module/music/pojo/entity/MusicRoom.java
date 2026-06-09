package com.createpen.fifteenhundun.module.music.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MusicRoom {
    //房间唯一标识 用户id 一个用户id只有一个房间
    private Long userId;
    //播放队列(存Music Id)
    private List<Long> MusicIdList;
    //当前播放的MusicId
    private Long currentMusicId;
    //房间秘钥(允许修改)
    private String roomSecret;

}
