package com.createpen.fifteenhundun.module.mood.mapper;

import com.createpen.fifteenhundun.module.mood.pojo.Entity.UserMoodHealth;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MoodHealthMapper {

    @Insert("INSERT INTO user_mood_health(user_id, date, mood, note) " +
            "VALUES (#{userId}, #{date}, #{mood}, #{note})")
    void insertMood(UserMoodHealth record);

    @Select("SELECT * FROM user_mood_health WHERE user_id = #{userId} AND date = #{date}")
    UserMoodHealth selectByUserIdAndDate(Long userId, LocalDate date);

    @Select("SELECT * FROM user_mood_health WHERE user_id = #{userId} ORDER BY date DESC")
    List<UserMoodHealth> selectAllByUserId(Long userId);

    @Update("UPDATE user_mood_health SET mood = #{mood}, note = #{note} " +
            "WHERE user_id = #{userId} AND date = #{date}")
    void updateMood(UserMoodHealth record);
}
