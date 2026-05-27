package com.createpen.fifteenhundun.module.place.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.exception.MyException.UserExistedException;
import com.createpen.fifteenhundun.module.place.cache.PlaceHealthRedisService;
import com.createpen.fifteenhundun.module.place.mapper.PlaceHealthMapper;
import com.createpen.fifteenhundun.module.place.pojo.DTO.PlaceHealthDTO;
import com.createpen.fifteenhundun.module.place.pojo.Entity.UserPlaceHealth;
import com.createpen.fifteenhundun.module.place.service.PlaceHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
public class PlaceHealthServiceImpl implements PlaceHealthService {

    private final PlaceHealthRedisService placeHealthRedisService;
    private final PlaceHealthMapper placeHealthMapper;

    public PlaceHealthServiceImpl(PlaceHealthRedisService placeHealthRedisService,
                                  PlaceHealthMapper placeHealthMapper) {
        this.placeHealthRedisService = placeHealthRedisService;
        this.placeHealthMapper = placeHealthMapper;
    }

    @Transactional
    @Override
    public int insertPlace(PlaceHealthDTO dto) {
        UserPlaceHealth record = dtoToEntity(dto);
        validateRecord(record);

        if (isPlaceExist(record)) {
            log.info("外出记录已存在，跳过插入, userId: {}, date: {}", record.getUserId(), record.getDate());
            return 0;
        }

        placeHealthMapper.insertPlace(record);
        placeHealthRedisService.save(record);
        log.info("外出记录已录入, userId: {}, date: {}", record.getUserId(), record.getDate());
        return 1;
    }

    @Transactional
    @Override
    public void updatePlace(PlaceHealthDTO dto) {
        UserPlaceHealth record = dtoToEntity(dto);
        validateRecord(record);

        if (!isPlaceExist(record)) {
            throw new UserExistedException("外出记录不存在，无法更新");
        }

        placeHealthMapper.updatePlace(record);
        placeHealthRedisService.delete(record.getUserId(), record.getDate());
        log.info("外出记录已更新, userId: {}, date: {}", record.getUserId(), record.getDate());
    }

    private UserPlaceHealth dtoToEntity(PlaceHealthDTO dto) {
        UserPlaceHealth record = new UserPlaceHealth();
        record.setUserId(BaseContext.getCurrentId());
        record.setDate(LocalDate.now());
        record.setLocation(dto.getLocation());
        record.setTransportMode(dto.getTransportMode());
        record.setSteps(dto.getSteps() != null ? dto.getSteps() : 0);
        return record;
    }

    private void validateRecord(UserPlaceHealth record) {
        if (record.getLocation() == null || record.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("地点不能为空");
        }
        Integer steps = record.getSteps();
        if (steps != null && (steps < 0 || steps > 100000)) {
            throw new IllegalArgumentException("步数须在 0~100000 之间");
        }
    }

    private boolean isPlaceExist(UserPlaceHealth record) {
        UserPlaceHealth cached = placeHealthRedisService.select(record.getUserId(), record.getDate());
        if (cached != null && cached.getId() != null) {
            log.info("外出记录缓存命中, userId: {}, date: {}", record.getUserId(), record.getDate());
            return true;
        }

        UserPlaceHealth fromDb = placeHealthMapper.selectByUserIdAndDate(record.getUserId(), record.getDate());
        if (fromDb != null && fromDb.getId() != null) {
            placeHealthRedisService.save(fromDb);
            return true;
        }
        return false;
    }
}
