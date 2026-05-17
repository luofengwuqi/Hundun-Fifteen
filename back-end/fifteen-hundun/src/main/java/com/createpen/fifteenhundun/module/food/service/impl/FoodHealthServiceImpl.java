package com.createpen.fifteenhundun.module.food.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.exception.MyException.UserExistedException;
import com.createpen.fifteenhundun.module.food.cache.FoodHealthRedisService;
import com.createpen.fifteenhundun.module.food.mapper.FoodHealthMapper;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodDTO;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFood;
import com.createpen.fifteenhundun.module.food.service.FoodHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
public class FoodHealthServiceImpl implements FoodHealthService {

    private final FoodHealthRedisService foodHealthRedisService;
    private final FoodHealthMapper foodHealthMapper;

    public FoodHealthServiceImpl(FoodHealthRedisService foodHealthRedisService,
                                 FoodHealthMapper foodHealthMapper) {
        this.foodHealthRedisService = foodHealthRedisService;
        this.foodHealthMapper = foodHealthMapper;
    }

    @Transactional
    @Override
    public int insertFood(DayFoodDTO dto) {
        // 对象转换
        DayFood dayFood = foodDTOToEntity(dto);
        // 校验
        if (isFoodExist(dayFood)) {
            log.info("饮食健康记录已存在，跳过插入");
            return 0;
        }
        // 数据库操作
        foodHealthMapper.insertFood(dayFood);
        // 缓存
        foodHealthRedisService.save(dayFood);
        log.info("饮食健康记录已录入");
        return 1; // 执行成功标识
    }

    // 校验是否存在
    private boolean isFoodExist(DayFood dayFood) {
        // 先查 Redis
        DayFood entityInRedis = foodHealthRedisService.select(dayFood.getUserId(), dayFood.getDate());
        if (entityInRedis != null && entityInRedis.getId() != null) {
            return true;
        }
        // 再查 MySQL
        DayFood entityInMysql = foodHealthMapper.selectFoodByUserIdAndDate(dayFood.getUserId(), dayFood.getDate());
        if (entityInMysql != null && entityInMysql.getId() != null) {
            // 回填缓存
            foodHealthRedisService.save(entityInMysql);
            return true;
        }
        return false;
    }

    // DTO转Entity
    private DayFood foodDTOToEntity(DayFoodDTO dto) {
        DayFood dayFood = new DayFood();
        dayFood.setUserId(BaseContext.getCurrentId());
        dayFood.setDate(LocalDate.now());
        dayFood.setMorningFood(dto.getMorningFood() == null ? "" : dto.getMorningFood());
        dayFood.setAfternoonFood(dto.getAfternoonFood() == null ? "" : dto.getAfternoonFood());
        dayFood.setEveningFood(dto.getEveningFood() == null ? "" : dto.getEveningFood());
        dayFood.setElseFood(dto.getElseFood() == null ? "" : dto.getElseFood());
        return dayFood;
    }

    @Transactional
    @Override
    public void updateFood(DayFoodDTO dto) {
        // 对象转换
        DayFood dayFood = foodDTOToEntity(dto);
        // 校验
        if (!isFoodExist(dayFood)) {
            throw new UserExistedException("饮食健康记录不存在，无法更新");
        }
        // 数据库操作
        foodHealthMapper.updateFood(dayFood);
        // 删除旧缓存（下次查询时会从数据库加载并重新缓存）
        foodHealthRedisService.delete(dayFood.getUserId(), dayFood.getDate());
        log.info("饮食健康记录已更新，缓存已清除");
    }
}
