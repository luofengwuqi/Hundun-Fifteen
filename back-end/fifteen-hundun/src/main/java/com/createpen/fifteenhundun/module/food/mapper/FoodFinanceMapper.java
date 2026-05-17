package com.createpen.fifteenhundun.module.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFoodPrice;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface FoodFinanceMapper extends BaseMapper<DayFoodPrice> {

    @Insert("INSERT INTO user_food_finance(user_id, date, morning_food_price, afternoon_food_price, evening_food_price, else_food_price, total_price) " +
            "VALUES (#{userId}, #{date}, #{morningFoodPrice}, #{afternoonFoodPrice}, #{eveningFoodPrice}, #{elseFoodPrice}, #{totalPrice})")
    void insertFoodMoney(DayFoodPrice dayFoodPrice);

    @Select("SELECT * FROM user_food_finance WHERE user_id = #{userId} AND date = #{date}")
    DayFoodPrice selectFoodPriceByUserIdAndDate(Long userId, LocalDate date);

    @Update("UPDATE user_food_finance SET morning_food_price = #{morningFoodPrice}, afternoon_food_price = #{afternoonFoodPrice}, evening_food_price = #{eveningFoodPrice}, else_food_price = #{elseFoodPrice},total_price=#{totalPrice} where user_id =#{userId} and date = #{date}")
    void updateFoodMoney(DayFoodPrice dayFoodPrice);

}
