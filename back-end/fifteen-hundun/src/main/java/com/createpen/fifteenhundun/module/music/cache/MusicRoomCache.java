package com.createpen.fifteenhundun.module.music.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.createpen.fifteenhundun.common.constant.redis.MusicRoomConstant.*;

/**
 * 音乐房间 Redis 缓存服务
 * 
 * <p>Redis 数据结构：</p>
 * <pre>
 * music
 *   |_____room:${userId} (房间标识)
 *   |             |________________current (当前播放的MusicId)
 *   |             |________________list (当前播放的队列)
 *   |_____secret:${password} (房间密码，value为房主ID)
 * user
 *   |_____musicRoom:${userId} (用户当前所在房间，指向房主ID)
 * </pre>
 *
 * <p>功能分类：</p>
 * <ul>
 *   <li><b>房间标识操作：</b>
 *     <ul>
 *       <li>{@link #createRoom(Long)} - 创建房间标识</li>
 *       <li>{@link #isRoomExist(Long)} - 检查房间是否存在</li>
 *       <li>{@link #deleteRoom(Long)} - 删除房间标识</li>
 *     </ul>
 *   </li>
 *   <li><b>当前播放音乐操作：</b>
 *     <ul>
 *       <li>{@link #setCurrentMusic(Long, Long)} - 设置当前播放的音乐ID</li>
 *       <li>{@link #getCurrentMusic(Long)} - 获取当前播放的音乐ID</li>
 *       <li>{@link #deleteCurrentMusic(Long)} - 删除当前播放音乐信息</li>
 *     </ul>
 *   </li>
 *   <li><b>播放列表操作：</b>
 *     <ul>
 *       <li>{@link #addToPlaylist(Long, Long)} - 向播放列表添加音乐</li>
 *       <li>{@link #removeFromPlaylist(Long, Long)} - 从播放列表移除音乐</li>
 *       <li>{@link #getPlaylist(Long)} - 获取完整播放列表</li>
 *       <li>{@link #getPlaylistSize(Long)} - 获取播放列表长度</li>
 *       <li>{@link #clearPlaylist(Long)} - 清空播放列表</li>
 *     </ul>
 *   </li>
 *   <li><b>房间密码操作：</b>
 *     <ul>
 *       <li>{@link #setRoomPassword(String, Long)} - 设置房间密码（记录密码与房主的映射）</li>
 *       <li>{@link #getOwnerIdByPassword(String)} - 通过密码获取房主ID</li>
 *       <li>{@link #verifyRoomPassword(String, Long)} - 验证房间密码是否正确</li>
 *       <li>{@link #deleteRoomPassword(String)} - 删除房间密码</li>
 *     </ul>
 *   </li>
 *   <li><b>用户所在房间操作：</b>
 *     <ul>
 *       <li>{@link #setUserInRoom(Long, Long)} - 设置用户当前所在房间（指向房主ID）</li>
 *       <li>{@link #getUserCurrentRoom(Long)} - 获取用户当前所在房间的房主ID</li>
 *       <li>{@link #isUserInRoom(Long)} - 检查用户是否在房间中</li>
 *       <li>{@link #userLeaveRoom(Long)} - 用户离开房间</li>
 *     </ul>
 *   </li>
 *   <li><b>批量清理操作：</b>
 *     <ul>
 *       <li>{@link #cleanupRoom(Long)} - 清理房间所有相关数据（房间标识、当前播放、播放列表、用户所在房间）</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>注意事项：</p>
 * <ul>
 *   <li>所有键默认过期时间为 3 小时（{@link com.createpen.fifteenhundun.common.constant.redis.MusicRoomConstant#ROOM_EXPIRED_TIME}）</li>
 *   <li>播放列表使用 List 结构，支持有序存储</li>
 *   <li>房间密码用于验证进入房间的权限</li>
 *   <li>用户所在房间记录的是房主ID，而非房间ID</li>
 * </ul>
 *
 * @author fifteenhundun
 * @see com.createpen.fifteenhundun.common.constant.redis.MusicRoomConstant
 */
@Component
@Slf4j
public class MusicRoomCache {
    private final RedisTemplate<String, Object> redisTemplate;

    public MusicRoomCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== 房间标识相关操作 ====================

    /**
     * 创建房间标识
     */
    public void createRoom(Long userId) {
        String key = buildMusicRoomKey(userId);
        redisTemplate.opsForValue().set(key, ROOM_EXIST, ROOM_EXPIRED_TIME, TimeUnit.SECONDS);
        log.info("创建音乐房间, userId={}", userId);
    }

    /**
     * 检查房间是否存在
     */
    public Boolean isRoomExist(Long userId) {
        String key = buildMusicRoomKey(userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 删除房间标识
     */
    public void deleteRoom(Long userId) {
        String key = buildMusicRoomKey(userId);
        redisTemplate.delete(key);
        log.info("删除音乐房间, userId={}", userId);
    }

    // ==================== 当前播放音乐相关操作 ====================

    /**
     * 设置当前播放的音乐ID
     */
    public void setCurrentMusic(Long userId, Long musicId) {
        String key = buildMusicRoomCurrentKey(userId);
        redisTemplate.opsForValue().set(key, musicId, ROOM_EXPIRED_TIME, TimeUnit.SECONDS);
        log.info("设置当前播放音乐, userId={}, musicId={}", userId, musicId);
    }

    /**
     * 获取当前播放的音乐ID
     */
    public Long getCurrentMusic(Long userId) {
        String key = buildMusicRoomCurrentKey(userId);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return value instanceof Long ? (Long) value : Long.parseLong(value.toString());
    }

    /**
     * 删除当前播放音乐信息
     */
    public void deleteCurrentMusic(Long userId) {
        String key = buildMusicRoomCurrentKey(userId);
        redisTemplate.delete(key);
        log.info("删除当前播放音乐, userId={}", userId);
    }

    // ==================== 播放列表相关操作 ====================

    /**
     * 向播放列表添加音乐
     */
    public void addToPlaylist(Long userId, Long musicId) {
        String key = buildMusicRoomListKey(userId);
        redisTemplate.opsForList().rightPush(key, musicId);
        redisTemplate.expire(key, ROOM_EXPIRED_TIME, TimeUnit.SECONDS);
        log.info("添加音乐到播放列表, userId={}, musicId={}", userId, musicId);
    }

    /**
     * 从播放列表移除音乐
     */
    public void removeFromPlaylist(Long userId, Long musicId) {
        String key = buildMusicRoomListKey(userId);
        redisTemplate.opsForList().remove(key, 1, musicId);
        log.info("从播放列表移除音乐, userId={}, musicId={}", userId, musicId);
    }

    /**
     * 获取播放列表
     */
    public List<Long> getPlaylist(Long userId) {
        String key = buildMusicRoomListKey(userId);
        List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);
        if (rawList == null || rawList.isEmpty()) {
            return List.of();
        }
        return rawList.stream()
                .map(v -> v instanceof Long ? (Long) v : Long.parseLong(v.toString()))
                .toList();
    }

    /**
     * 获取播放列表长度
     */
    public Long getPlaylistSize(Long userId) {
        String key = buildMusicRoomListKey(userId);
        Long size = redisTemplate.opsForList().size(key);
        return size == null ? 0L : size;
    }

    /**
     * 清空播放列表
     */
    public void clearPlaylist(Long userId) {
        String key = buildMusicRoomListKey(userId);
        redisTemplate.delete(key);
        log.info("清空播放列表, userId={}", userId);
    }

    // ==================== 房间密码相关操作 ====================

    /**
     * 设置房间密码（记录密码与房主的映射）
     */
    public void setRoomPassword(String password, Long ownerId) {
        String key = buildMusicRoomSecret(password);
        redisTemplate.opsForValue().set(key, ownerId, ROOM_EXPIRED_TIME, TimeUnit.SECONDS);
        String reverseKey = buildMusicRoomPasswordKey(ownerId);
        redisTemplate.opsForValue().set(reverseKey, password, ROOM_EXPIRED_TIME, TimeUnit.SECONDS);
        log.info("设置房间密码, password={}, ownerId={}", password, ownerId);
    }

    /**
     * 通过密码获取房主ID
     */
    public Long getOwnerIdByPassword(String password) {
        String key = buildMusicRoomSecret(password);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return value instanceof Long ? (Long) value : Long.parseLong(value.toString());
    }

    /**
     * 验证房间密码是否正确
     */
    public Boolean verifyRoomPassword(String password, Long expectedOwnerId) {
        Long ownerId = getOwnerIdByPassword(password);
        return ownerId != null && ownerId.equals(expectedOwnerId);
    }

    /**
     * 删除房间密码
     */
    public void deleteRoomPassword(String password) {
        String key = buildMusicRoomSecret(password);
        redisTemplate.delete(key);
        log.info("删除房间密码, password={}", password);
    }

    /**
     * 通过房主ID删除房间密码（反向映射）
     */
    public void deleteRoomPasswordByUserId(Long userId) {
        String reverseKey = buildMusicRoomPasswordKey(userId);
        Object passwordObj = redisTemplate.opsForValue().get(reverseKey);
        if (passwordObj != null) {
            String password = passwordObj.toString();
            deleteRoomPassword(password);
        }
        redisTemplate.delete(reverseKey);
        log.info("删除房间密码反向映射, userId={}", userId);
    }

    // ==================== 用户所在房间相关操作 ====================

    /**
     * 设置用户当前所在房间（指向房主ID）
     */
    public void setUserInRoom(Long userId, Long roomId) {
        String key = buildUserMusicRoom(userId);
        redisTemplate.opsForValue().set(key, roomId, ROOM_EXPIRED_TIME, TimeUnit.SECONDS);
        log.info("设置用户所在房间, userId={}, roomId={}", userId, roomId);
    }

    /**
     * 获取用户当前所在房间的房主ID
     */
    public Long getUserCurrentRoom(Long userId) {
        String key = buildUserMusicRoom(userId);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return value instanceof Long ? (Long) value : Long.parseLong(value.toString());
    }

    /**
     * 检查用户是否在房间中
     */
    public Boolean isUserInRoom(Long userId) {
        String key = buildUserMusicRoom(userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 用户离开房间
     */
    public void userLeaveRoom(Long userId) {
        String key = buildUserMusicRoom(userId);
        redisTemplate.delete(key);
        log.info("用户离开房间, userId={}", userId);
    }

    // ==================== 批量清理操作 ====================

    /**
     * 清理房间所有相关数据
     */
    public void cleanupRoom(Long userId) {
        deleteRoom(userId);
        deleteCurrentMusic(userId);
        clearPlaylist(userId);
        userLeaveRoom(userId);
        deleteRoomPasswordByUserId(userId);
        log.info("清理房间所有数据, userId={}", userId);
    }
}
