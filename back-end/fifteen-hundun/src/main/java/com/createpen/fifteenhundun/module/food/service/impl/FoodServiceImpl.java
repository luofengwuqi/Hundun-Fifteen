package com.createpen.fifteenhundun.module.food.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.exception.MyException.UserExistedException;
import com.createpen.fifteenhundun.module.food.mapper.FoodMapper;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodDTO;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodPriceDTO;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFood;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFoodPrice;
import com.createpen.fifteenhundun.module.food.service.FoodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
public class FoodServiceImpl implements FoodService {
    private final FoodMapper foodMapper;

    public FoodServiceImpl(FoodMapper foodMapper) {
        this.foodMapper = foodMapper;
    }
    /*
        健康
    */

    @Override
    public int insertFood(DayFoodDTO dto) {
        //对象
        DayFood dayFood = foodDTOToEntity(dto);
        //校验
        if(isFoodExist(dayFood)){
            log.info("记录已存在");
            return 0;
        }
        //数据库
        foodMapper.insertFood(dayFood);
        return 1;//执行成功标识
    }
    //校验
    private boolean isFoodExist(DayFood dayFood) {
        DayFood entityInMysql = foodMapper.selectFoodByUserIdAndDate(dayFood.getUserId(),dayFood.getDate());
        return entityInMysql != null && entityInMysql.getId() != null;
    }

    //对象
    private DayFood foodDTOToEntity(DayFoodDTO dto){
        DayFood dayFood = new DayFood();
        dayFood.setUserId(BaseContext.getCurrentId());
        dayFood.setDate(LocalDate.now());
        dayFood.setMorningFood(dto.getMorningFood()==null ? "" : dto.getMorningFood());
        dayFood.setAfternoonFood(dto.getAfternoonFood()==null ? "" : dto.getAfternoonFood());
        dayFood.setEveningFood(dto.getEveningFood()==null ? "" : dto.getEveningFood());
        dayFood.setElseFood(dto.getElseFood()==null ? "" : dto.getElseFood());
        return dayFood;
    }
    /*
        财务代码
    */
    /*
        插入相关
    */
    @Transactional
    @Override
    public Integer insertFoodMoney(DayFoodPriceDTO dto) {
        //对象
        DayFoodPrice dayFoodPrice = priceDTOToEntity(dto);
        //校验
        if(isFoodPriceExist(dayFoodPrice)){
            log.info("记录已存在");
            return 0;//已存在标识
        }
        //数据库
        foodMapper.insertFoodMoney(dayFoodPrice);
        log.info("记录已录入");
        return 1;//执行成功标识
    }
    //校验
    private boolean isFoodPriceExist(DayFoodPrice dayFoodPrice) {
        DayFoodPrice entityInMysql = foodMapper.selectFoodPriceByUserIdAndDate(dayFoodPrice.getUserId(), dayFoodPrice.getDate());
        return entityInMysql != null && entityInMysql.getId() != null;
    }
    //对象
      private DayFoodPrice priceDTOToEntity(DayFoodPriceDTO dto){
        DayFoodPrice dayFoodPrice = new DayFoodPrice();
        dayFoodPrice.setUserId(BaseContext.getCurrentId());
        dayFoodPrice.setDate(LocalDate.now());
          dayFoodPrice.setMorningFoodPrice(
                  dto.getMorningFoodPrice() != null ?
                          dto.getMorningFoodPrice().multiply(BigDecimal.valueOf(100)).intValue() : 0
          );
          dayFoodPrice.setAfternoonFoodPrice(
                  dto.getAfternoonFoodPrice() != null ?
                          dto.getAfternoonFoodPrice().multiply(BigDecimal.valueOf(100)).intValue() : 0
          );
          dayFoodPrice.setEveningFoodPrice(
                  dto.getEveningFoodPrice() != null ?
                          dto.getEveningFoodPrice().multiply(BigDecimal.valueOf(100)).intValue() : 0
          );
          dayFoodPrice.setElseFoodPrice(
                  dto.getElseFoodPrice() != null ?
                          dto.getElseFoodPrice().multiply(BigDecimal.valueOf(100)).intValue() : 0
          );
        return dayFoodPrice;
      }
    /*
        更新相关
      */
    @Transactional
    @Override
    public void updateFoodMoney(DayFoodPriceDTO dto) {
        //对象
        DayFoodPrice dayFoodPrice = priceDTOToEntity(dto);
        //校验
        if(!isFoodPriceExist(dayFoodPrice)){
            log.warn("记录不存在");
            throw new UserExistedException("记录不存在");
        }
        //数据库
        foodMapper.updateFoodMoney(dayFoodPrice);
        log.info("记录已更新");
    }


}
