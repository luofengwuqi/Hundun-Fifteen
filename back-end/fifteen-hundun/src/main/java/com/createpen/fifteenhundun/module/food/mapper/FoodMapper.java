package com.createpen.fifteenhundun.module.food.mapper;

import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFood;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFoodPrice;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface FoodMapper {

    @Insert("INSERT INTO user_food_finance(user_id, date, morning_food_price, afternoon_food_price, evening_food_price, else_food_price) " +
            "VALUES (#{userId}, #{date}, #{morningFoodPrice}, #{afternoonFoodPrice}, #{eveningFoodPrice}, #{elseFoodPrice})")
    void insertFoodMoney(DayFoodPrice dayFoodPrice);

    @Insert("INSERT INTO user_food_health(user_id, date, morning_food, afternoon_food, evening_food, else_food) " +
            "VALUES (#{userId}, #{date}, #{morningFood}, #{afternoonFood}, #{eveningFood}, #{elseFood})")
    void insertFood(DayFood dayFood);

    @Select("SELECT * FROM user_food_finance WHERE user_id = #{userId} AND date = #{date}")
    DayFoodPrice selectFoodPriceByUserIdAndDate(Long userId, LocalDate date);

    @Update("UPDATE user_food_finance SET morning_food_price = #{morningFoodPrice}, afternoon_food_price = #{afternoonFoodPrice}, evening_food_price = #{eveningFoodPrice}, else_food_price = #{elseFoodPrice} where user_id =#{userId} and date = #{date}" )
    void updateFoodMoney(DayFoodPrice dayFoodPrice);

    @Select("SELECT * FROM user_food_health WHERE user_id = #{userId} AND date = #{date}")
    DayFood selectFoodByUserIdAndDate(Long userId, LocalDate date);

    @Update("UPDATE user_food_health SET morning_food = #{morningFood}, afternoon_food = #{afternoonFood}, evening_food = #{eveningFood}, else_food = #{elseFood} where user_id =#{userId} and date = #{date}" )
    void updateFood(DayFood dayFood);

}
