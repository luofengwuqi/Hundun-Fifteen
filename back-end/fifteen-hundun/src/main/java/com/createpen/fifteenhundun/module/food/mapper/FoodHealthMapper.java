package com.createpen.fifteenhundun.module.food.mapper;

import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFood;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface FoodHealthMapper {

    @Insert("INSERT INTO user_food_health(user_id, date, morning_food, afternoon_food, evening_food, else_food) " +
            "VALUES (#{userId}, #{date}, #{morningFood}, #{afternoonFood}, #{eveningFood}, #{elseFood})")
    void insertFood(DayFood dayFood);

    @Select("SELECT * FROM user_food_health WHERE user_id = #{userId} AND date = #{date}")
    DayFood selectFoodByUserIdAndDate(Long userId, LocalDate date);

    @Update("UPDATE user_food_health SET morning_food = #{morningFood}, afternoon_food = #{afternoonFood}, evening_food = #{eveningFood}, else_food = #{elseFood} where user_id =#{userId} and date = #{date}")
    void updateFood(DayFood dayFood);

}
