package com.createpen.fifteenhundun.module.place.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.createpen.fifteenhundun.module.place.pojo.Entity.UserPlaceFinance;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface PlaceFinanceMapper extends BaseMapper<UserPlaceFinance> {

    @Insert("INSERT INTO user_place_finance(user_id, date, transport_cost, transport_mode, location) " +
            "VALUES (#{userId}, #{date}, #{transportCost}, #{transportMode}, #{location})")
    void insertPlaceFinance(UserPlaceFinance record);

    @Select("SELECT * FROM user_place_finance WHERE user_id = #{userId} AND date = #{date}")
    UserPlaceFinance selectByUserIdAndDate(Long userId, LocalDate date);

    @Update("UPDATE user_place_finance SET transport_cost = #{transportCost}, " +
            "transport_mode = #{transportMode}, location = #{location} " +
            "WHERE user_id = #{userId} AND date = #{date}")
    void updatePlaceFinance(UserPlaceFinance record);
}
