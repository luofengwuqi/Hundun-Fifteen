package com.createpen.fifteenhundun.common.constant.redis;

public class MusicRoomConstant {
    /*
        music
          |_____room:${userId}(房间标识)
          |             |________________current(当前播放的MusicId)
          |             |________________list(当前播放的队列)
          |_____secret:${password}(房间密码)
        user
          |_____musicRoom:${userId}(用户当前所在房间)1
    */

    public static final String MUSIC_ROOM_KEY = "music:room:";
    public static final Integer ROOM_EXIST = 1 ;
    //过期:三小时
    public static final Integer ROOM_EXPIRED_TIME = 3*60*60;

    //音乐房间Key music:room:${userId}
    public static  String buildMusicRoomKey(Long userId){
        return  MUSIC_ROOM_KEY + userId;
    }
    //音乐房间当前播放Key music:room:${userId}:current
    public static String buildMusicRoomCurrentKey(Long userId) {
        return MUSIC_ROOM_KEY + userId + ":current";
    }
    //音乐房间播放列表Key music:room:${userId}:list
    public static String buildMusicRoomListKey(Long userId) {
        return MUSIC_ROOM_KEY + userId + ":list";
    }
    //房间秘钥
    //key: music:secret:${password}
    //value: 创建者id
    public static String buildMusicRoomSecret(String password){
        return "music:secret:"+password;
    }
    //用户当前所在房间 user:musicRoom:${userId}
    //注:这个userId不一定是自己的 可能是别人的 指向的是房主的userId
    public static String buildUserMusicRoom(Long userId){
        return "user:musicRoom:"+userId;
    }
    //房间密码反向映射 music:room:${userId}:password
    //value: password(明文) 用于清理时通过userId找到密码并删除secret
    public static String buildMusicRoomPasswordKey(Long userId){
        return MUSIC_ROOM_KEY + userId + ":password";
    }
}
