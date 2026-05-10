package com.createpen.fifteenhundun.module.food.mapper;

import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFood;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFoodPrice;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FoodMapper {

    @Insert("INSERT INTO user_food_finance(user_id, date, morning_good_food_price, afternoon_food_price, evening_food_price, else_food_price) " +
            "VALUES (#{userId}, #{date}, #{morningGoodFoodPrice}, #{afternoonFoodPrice}, #{eveningFoodPrice}, #{elseFoodPrice})")
    void insertFoodMoney(DayFoodPrice dayFoodPrice);

    @Insert("INSERT INTO user_food_health(user_id, date, morning_good_food, afternoon_food, evening_food, else_food) " +
            "VALUES (#{userId}, #{date}, #{morningGoodFood}, #{afternoonFood}, #{eveningFood}, #{elseFood})")
    void insertFood(DayFood dayFood);
}
