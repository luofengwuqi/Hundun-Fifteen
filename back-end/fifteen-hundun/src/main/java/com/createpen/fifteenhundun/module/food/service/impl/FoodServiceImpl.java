package com.createpen.fifteenhundun.module.food.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.module.food.mapper.FoodMapper;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodDTO;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodPriceDTO;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFood;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFoodPrice;
import com.createpen.fifteenhundun.module.food.service.FoodService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class FoodServiceImpl implements FoodService {
    @Autowired
    private FoodMapper foodMapper;

    @Override
    public void insertFood(DayFoodDTO dto) {
        //校验?
        //对象
        DayFood dayFood = new DayFood();
        BeanUtils.copyProperties(dto, dayFood);
        dayFood.setUserId(BaseContext.getCurrentId());
        dayFood.setDate(LocalDate.now());
        //数据库
        foodMapper.insertFood(dayFood);
    }

    @Override
    public void insertFoodMoney(DayFoodPriceDTO dto) {
        //校验?
        //对象
        DayFoodPrice dayFoodPrice = new DayFoodPrice();
        BeanUtils.copyProperties(dto, dayFoodPrice);
        dayFoodPrice.setUserId(BaseContext.getCurrentId());
        dayFoodPrice.setDate(LocalDate.now());
        //数据库
        foodMapper.insertFoodMoney(dayFoodPrice);
    }


}
