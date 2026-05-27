package com.createpen.fifteenhundun.module.sleep.mapper;

import com.createpen.fifteenhundun.module.sleep.pojo.Entity.UserSleepHealth;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SleepHealthMapper {

    @Insert("INSERT INTO user_sleep_health(user_id, date, quality, duration_min) " +
            "VALUES (#{userId}, #{date}, #{quality}, #{durationMin})")
    void insertSleep(UserSleepHealth record);

    @Select("SELECT * FROM user_sleep_health WHERE user_id = #{userId} AND date = #{date}")
    UserSleepHealth selectByUserIdAndDate(Long userId, LocalDate date);

    @Select("SELECT * FROM user_sleep_health WHERE user_id = #{userId} ORDER BY date DESC")
    List<UserSleepHealth> selectAllByUserId(Long userId);

    @Update("UPDATE user_sleep_health SET quality = #{quality}, duration_min = #{durationMin} " +
            "WHERE user_id = #{userId} AND date = #{date}")
    void updateSleep(UserSleepHealth record);
}
