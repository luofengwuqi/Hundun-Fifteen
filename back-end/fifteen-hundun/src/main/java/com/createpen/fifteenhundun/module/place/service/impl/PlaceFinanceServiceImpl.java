package com.createpen.fifteenhundun.module.place.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.createpen.fifteenhundun.common.constant.finance.FinanceDetailConstant;
import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.exception.MyException.UserExistedException;
import com.createpen.fifteenhundun.module.place.cache.PlaceFinanceRedisService;
import com.createpen.fifteenhundun.module.place.mapper.PlaceFinanceMapper;
import com.createpen.fifteenhundun.module.place.pojo.DTO.PlaceFinanceDTO;
import com.createpen.fifteenhundun.module.place.pojo.Entity.UserPlaceFinance;
import com.createpen.fifteenhundun.module.place.service.PlaceFinanceService;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;
import com.createpen.fifteenhundun.module.selecter.provider.FinanceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PlaceFinanceServiceImpl implements PlaceFinanceService, FinanceProvider {

    private final PlaceFinanceRedisService placeFinanceRedisService;
    private final PlaceFinanceMapper placeFinanceMapper;

    public PlaceFinanceServiceImpl(PlaceFinanceRedisService placeFinanceRedisService,
                                   PlaceFinanceMapper placeFinanceMapper) {
        this.placeFinanceRedisService = placeFinanceRedisService;
        this.placeFinanceMapper = placeFinanceMapper;
    }

    @Transactional
    @Override
    public int insertPlaceFinance(PlaceFinanceDTO dto) {
        UserPlaceFinance record = dtoToEntity(dto);

        if (isPlaceFinanceExist(record)) {
            log.info("外出财务记录已存在，跳过插入, userId: {}, date: {}", record.getUserId(), record.getDate());
            return 0;
        }

        placeFinanceMapper.insertPlaceFinance(record);
        placeFinanceRedisService.save(record);
        log.info("外出财务记录已录入, userId: {}, date: {}", record.getUserId(), record.getDate());
        return 1;
    }

    @Transactional
    @Override
    public void updatePlaceFinance(PlaceFinanceDTO dto) {
        UserPlaceFinance record = dtoToEntity(dto);

        if (!isPlaceFinanceExist(record)) {
            throw new UserExistedException("外出财务记录不存在，无法更新");
        }

        placeFinanceMapper.updatePlaceFinance(record);
        placeFinanceRedisService.delete(record.getUserId(), record.getDate());
        log.info("外出财务记录已更新, userId: {}, date: {}", record.getUserId(), record.getDate());
    }

    private UserPlaceFinance dtoToEntity(PlaceFinanceDTO dto) {
        UserPlaceFinance record = new UserPlaceFinance();
        record.setUserId(BaseContext.getCurrentId());
        record.setDate(LocalDate.now());
        if (dto.getTransportCost() != null) {
            record.setTransportCost(dto.getTransportCost().multiply(BigDecimal.valueOf(100)).intValue());
        }
        record.setTransportMode(dto.getTransportMode());
        record.setLocation(dto.getLocation());
        return record;
    }

    private boolean isPlaceFinanceExist(UserPlaceFinance record) {
        UserPlaceFinance cached = placeFinanceRedisService.select(record.getUserId(), record.getDate());
        if (cached != null && cached.getId() != null) {
            log.info("外出财务记录缓存命中, userId: {}, date: {}", record.getUserId(), record.getDate());
            return true;
        }

        UserPlaceFinance fromDb = placeFinanceMapper.selectByUserIdAndDate(record.getUserId(), record.getDate());
        if (fromDb != null && fromDb.getId() != null) {
            placeFinanceRedisService.save(fromDb);
            return true;
        }
        return false;
    }

    // ========== FinanceProvider ==========

    @Override
    public Map<LocalDate, Integer> getFinanceSimpleData() {
        LambdaQueryWrapper<UserPlaceFinance> wrapper = new LambdaQueryWrapper<UserPlaceFinance>()
                .select(UserPlaceFinance::getDate, UserPlaceFinance::getTransportCost)
                .eq(UserPlaceFinance::getUserId, BaseContext.getCurrentId());
        List<UserPlaceFinance> list = placeFinanceMapper.selectList(wrapper);
        Map<LocalDate, Integer> map = new LinkedHashMap<>();
        for (UserPlaceFinance entity : list) {
            if (entity.getTransportCost() != null) {
                map.put(entity.getDate(), entity.getTransportCost());
            }
        }
        return map;
    }

    @Override
    public FinanceDetailVo getFinanceDetailData(LocalDate date) {
        Long currentId = BaseContext.getCurrentId();
        UserPlaceFinance entity = getByUserIdAndDate(currentId, date);
        if (entity == null || entity.getId() == null || entity.getTransportCost() == null) {
            return null;
        }
        FinanceDetailVo vo = new FinanceDetailVo();
        vo.setTitle(FinanceDetailConstant.FINANCE_TITLE_PLACE);
        Map<String, Integer> dataMap = new LinkedHashMap<>();
        dataMap.put(entity.getTransportMode()+":"+entity.getLocation(), entity.getTransportCost());
        vo.setDataMap(dataMap);
        return vo;
    }

    private UserPlaceFinance getByUserIdAndDate(Long userId, LocalDate date) {
        UserPlaceFinance cached = placeFinanceRedisService.select(userId, date);
        if (cached != null && cached.getId() != null) {
            return cached;
        }
        UserPlaceFinance fromDb = placeFinanceMapper.selectByUserIdAndDate(userId, date);
        if (fromDb != null && fromDb.getId() != null) {
            placeFinanceRedisService.save(fromDb);
        }
        return fromDb;
    }
}
