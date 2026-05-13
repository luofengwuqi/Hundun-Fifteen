package com.createpen.fifteenhundun.module.food.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.exception.MyException.UserExistedException;
import com.createpen.fifteenhundun.module.food.cache.FoodFinanceRedisService;
import com.createpen.fifteenhundun.module.food.cache.FoodHealthRedisService;
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
    private final FoodFinanceRedisService foodFinanceRedisService;
    private final FoodHealthRedisService foodHealthRedisService;
    private final FoodMapper foodMapper;

    public FoodServiceImpl(FoodFinanceRedisService foodFinanceRedisService, 
                          FoodHealthRedisService foodHealthRedisService,
                          FoodMapper foodMapper) {
        this.foodFinanceRedisService = foodFinanceRedisService;
        this.foodHealthRedisService = foodHealthRedisService;
        this.foodMapper = foodMapper;
    }
    /*
        健康
    */

    @Transactional
    @Override
    public int insertFood(DayFoodDTO dto) {
        //对象
        DayFood dayFood = foodDTOToEntity(dto);
        //校验
        if(isFoodExist(dayFood)){
            log.info("饮食健康记录已存在，跳过插入");
            return 0;
        }
        //数据库
        foodMapper.insertFood(dayFood);
        //缓存
        foodHealthRedisService.save(dayFood);
        log.info("饮食健康记录已录入");
        return 1;//执行成功标识
    }
    //校验
    private boolean isFoodExist(DayFood dayFood) {
        //先查 Redis
        DayFood entityInRedis = foodHealthRedisService.select(dayFood.getUserId(), dayFood.getDate());
        if (entityInRedis != null && entityInRedis.getId() != null){
            return true;
        }
        //再查 MySQL
        DayFood entityInMysql = foodMapper.selectFoodByUserIdAndDate(dayFood.getUserId(),dayFood.getDate());
        if (entityInMysql != null && entityInMysql.getId() != null){
            //回填缓存
            foodHealthRedisService.save(entityInMysql);
            return true;
        }
        return false;
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
            log.info("饮食费用记录已存在，跳过插入");
            return 0;//已存在标识
        }
        //数据库
        foodMapper.insertFoodMoney(dayFoodPrice);
        foodFinanceRedisService.save(dayFoodPrice);
        log.info("饮食费用记录已录入");
        return 1;//执行成功标识
    }
    //校验
    private boolean isFoodPriceExist(DayFoodPrice dayFoodPrice) {
        DayFoodPrice entityInRedis = foodFinanceRedisService.select(dayFoodPrice.getUserId(), dayFoodPrice.getDate());
        if (entityInRedis != null && entityInRedis.getId() != null){
            return true;
        }
        DayFoodPrice entityInMysql = foodMapper.selectFoodPriceByUserIdAndDate(dayFoodPrice.getUserId(), dayFoodPrice.getDate());
        if (entityInMysql != null && entityInMysql.getId() != null){
            foodFinanceRedisService.save(entityInMysql);
            return true;
        }
        return false;
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
            log.warn("饮食费用记录不存在，无法更新");
            throw new UserExistedException("饮食费用记录不存在，无法更新");
        }
        //数据库
        foodMapper.updateFoodMoney(dayFoodPrice);
        foodFinanceRedisService.delete(dayFoodPrice.getUserId(), dayFoodPrice.getDate());
        log.info("饮食费用记录已更新，缓存已清除");
    }

    @Transactional
    @Override
    public void updateFood(DayFoodDTO dto) {
        //对象
        DayFood dayFood = foodDTOToEntity(dto);
        //校验
        if (!isFoodExist(dayFood)) {
            throw new UserExistedException("饮食健康记录不存在，无法更新");
        }
        //数据库
        foodMapper.updateFood(dayFood);
        //删除旧缓存（下次查询时会从数据库加载并重新缓存）
        foodHealthRedisService.delete(dayFood.getUserId(), dayFood.getDate());
        log.info("饮食健康记录已更新，缓存已清除");
    }


}
