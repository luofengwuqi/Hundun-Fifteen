package com.createpen.fifteenhundun.module.food.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.exception.MyException.UserExistedException;
import com.createpen.fifteenhundun.module.food.cache.FoodFinanceRedisService;
import com.createpen.fifteenhundun.module.food.mapper.FoodFinanceMapper;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodPriceDTO;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFoodPrice;
import com.createpen.fifteenhundun.module.food.service.FoodFinanceService;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;
import com.createpen.fifteenhundun.module.selecter.provider.FinanceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class FoodFinanceServiceImpl implements FoodFinanceService, FinanceProvider {

    private final FoodFinanceRedisService foodFinanceRedisService;
    private final FoodFinanceMapper foodFinanceMapper;

    public FoodFinanceServiceImpl(FoodFinanceRedisService foodFinanceRedisService,
                                  FoodFinanceMapper foodFinanceMapper) {
        this.foodFinanceRedisService = foodFinanceRedisService;
        this.foodFinanceMapper = foodFinanceMapper;
    }

    @Transactional
    @Override
    public Integer insertFoodMoney(DayFoodPriceDTO dto) {
        // 对象转换
        DayFoodPrice dayFoodPrice = priceDTOToEntity(dto);
        // 校验
        if (isFoodPriceExist(dayFoodPrice)) {
            log.info("饮食费用记录已存在，跳过插入");
            return 0; // 已存在标识
        }
        // 数据库操作
        foodFinanceMapper.insertFoodMoney(dayFoodPrice);
        // 缓存
        foodFinanceRedisService.save(dayFoodPrice);
        log.info("饮食费用记录已录入");
        return 1; // 执行成功标识
    }

    // 校验是否存在
    private boolean isFoodPriceExist(DayFoodPrice dayFoodPrice) {
        DayFoodPrice entity = getDayFoodPriceByUserIdAndDate(dayFoodPrice.getUserId(), dayFoodPrice.getDate());
        return entity != null && entity.getId() != null;
    }

    // 根据用户ID和日期获取饮食费用记录
    public DayFoodPrice getDayFoodPriceByUserIdAndDate(Long userId, LocalDate date) {
        DayFoodPrice entityInRedis = foodFinanceRedisService.select(userId, date);
        if (entityInRedis != null && entityInRedis.getId() != null) {
            return entityInRedis;
        }
        DayFoodPrice entityInMysql = foodFinanceMapper.selectFoodPriceByUserIdAndDate(userId, date);
        if (entityInMysql != null && entityInMysql.getId() != null) {
            foodFinanceRedisService.save(entityInMysql);
            return entityInMysql;
        }
        return null;
    }

    // DTO转Entity
    private DayFoodPrice priceDTOToEntity(DayFoodPriceDTO dto) {
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
        Integer totalPrice = dayFoodPrice.getMorningFoodPrice() + dayFoodPrice.getAfternoonFoodPrice() 
                + dayFoodPrice.getEveningFoodPrice() + dayFoodPrice.getElseFoodPrice();
        dayFoodPrice.setTotalPrice(totalPrice);
        return dayFoodPrice;
    }

    @Transactional
    @Override
    public void updateFoodMoney(DayFoodPriceDTO dto) {
        // 对象转换
        DayFoodPrice dayFoodPrice = priceDTOToEntity(dto);
        // 校验
        if (!isFoodPriceExist(dayFoodPrice)) {
            log.warn("饮食费用记录不存在，无法更新");
            throw new UserExistedException("饮食费用记录不存在，无法更新");
        }
        // 数据库操作
        foodFinanceMapper.updateFoodMoney(dayFoodPrice);
        // 删除缓存
        foodFinanceRedisService.delete(dayFoodPrice.getUserId(), dayFoodPrice.getDate());
        log.info("饮食费用记录已更新，缓存已清除");
    }

    @Override
    public Map<String, Integer> getFinanceSimpleData() {
        // TODO: 实现获取简单财务数据的逻辑getFinanceSimpleData

        return Map.of();
    }

    @Override
    public FinanceDetailVo getFinanceDetailData(LocalDate date) {
        Long currentId = BaseContext.getCurrentId();
        DayFoodPrice entity = getDayFoodPriceByUserIdAndDate(currentId, date);
        if (entity == null || entity.getId() == null) {
            // 缓存空对象 防止穿透
            foodFinanceRedisService.saveEmpty(currentId, date);
            return null;
        }
        return dayFoodPriceToFinanceDetailVo(entity);
    }

    // 辅助方法：对象转换
    private FinanceDetailVo dayFoodPriceToFinanceDetailVo(DayFoodPrice entity) {
        if (entity == null) {
            return null;
        }
        FinanceDetailVo vo = new FinanceDetailVo();
        // title
        vo.setTitle("饮食");
        // detail Map
        Map<String, Integer> dataMap = new LinkedHashMap<>();
        if (entity.getMorningFoodPrice() != null && entity.getMorningFoodPrice() > 0) {
            dataMap.put("早餐", entity.getMorningFoodPrice());
        }
        if (entity.getAfternoonFoodPrice() != null && entity.getAfternoonFoodPrice() > 0) {
            dataMap.put("午餐", entity.getAfternoonFoodPrice());
        }
        if (entity.getEveningFoodPrice() != null && entity.getEveningFoodPrice() > 0) {
            dataMap.put("晚餐", entity.getEveningFoodPrice());
        }
        if (entity.getElseFoodPrice() != null && entity.getElseFoodPrice() > 0) {
            dataMap.put("其他", entity.getElseFoodPrice());
        }
        vo.setDataMap(dataMap);
        return vo;
    }
}
