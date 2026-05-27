package com.createpen.fifteenhundun.module.place.mapper;

import com.createpen.fifteenhundun.module.place.pojo.Entity.UserPlaceHealth;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface PlaceHealthMapper {

    @Insert("INSERT INTO user_place_health(user_id, date, location, transport_mode, steps) " +
            "VALUES (#{userId}, #{date}, #{location}, #{transportMode}, #{steps})")
    void insertPlace(UserPlaceHealth record);

    @Select("SELECT * FROM user_place_health WHERE user_id = #{userId} AND date = #{date}")
    UserPlaceHealth selectByUserIdAndDate(Long userId, LocalDate date);

    @Update("UPDATE user_place_health SET location = #{location}, transport_mode = #{transportMode}, " +
            "steps = #{steps} WHERE user_id = #{userId} AND date = #{date}")
    void updatePlace(UserPlaceHealth record);
}
