package com.createpen.fifteenhundun.module.sleep.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.exception.MyException.UserExistedException;
import com.createpen.fifteenhundun.module.sleep.cache.SleepHealthRedisService;
import com.createpen.fifteenhundun.module.sleep.mapper.SleepHealthMapper;
import com.createpen.fifteenhundun.module.sleep.pojo.DTO.SleepHealthDTO;
import com.createpen.fifteenhundun.module.sleep.pojo.Entity.UserSleepHealth;
import com.createpen.fifteenhundun.module.sleep.service.SleepHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
public class SleepHealthServiceImpl implements SleepHealthService {

    private final SleepHealthRedisService sleepHealthRedisService;
    private final SleepHealthMapper sleepHealthMapper;

    public SleepHealthServiceImpl(SleepHealthRedisService sleepHealthRedisService,
                                  SleepHealthMapper sleepHealthMapper) {
        this.sleepHealthRedisService = sleepHealthRedisService;
        this.sleepHealthMapper = sleepHealthMapper;
    }

    @Transactional
    @Override
    public int insertSleep(SleepHealthDTO dto) {
        UserSleepHealth record = dtoToEntity(dto);
        validateRecord(record);

        if (isSleepExist(record)) {
            log.info("睡眠记录已存在，跳过插入, userId: {}, date: {}", record.getUserId(), record.getDate());
            return 0;
        }

        sleepHealthMapper.insertSleep(record);
        sleepHealthRedisService.save(record);
        log.info("睡眠记录已录入, userId: {}, date: {}", record.getUserId(), record.getDate());
        return 1;
    }

    @Transactional
    @Override
    public void updateSleep(SleepHealthDTO dto) {
        UserSleepHealth record = dtoToEntity(dto);
        validateRecord(record);

        if (!isSleepExist(record)) {
            throw new UserExistedException("睡眠记录不存在，无法更新");
        }

        sleepHealthMapper.updateSleep(record);
        sleepHealthRedisService.delete(record.getUserId(), record.getDate());
        log.info("睡眠记录已更新, userId: {}, date: {}", record.getUserId(), record.getDate());
    }

    private UserSleepHealth dtoToEntity(SleepHealthDTO dto) {
        UserSleepHealth record = new UserSleepHealth();
        record.setUserId(BaseContext.getCurrentId());
        record.setDate(LocalDate.now());
        record.setQuality(dto.getQuality());
        record.setDurationMin(dto.getDurationMin());
        return record;
    }

    private void validateRecord(UserSleepHealth record) {
        if (record.getQuality() == null || record.getQuality() < 0 || record.getQuality() > 2) {
            throw new IllegalArgumentException("睡眠质量须为 0~2");
        }
        Integer duration = record.getDurationMin();
        if (duration != null && (duration < 0 || duration > 1440)) {
            throw new IllegalArgumentException("睡眠时长须在 0~1440 分钟之间");
        }
    }

    private boolean isSleepExist(UserSleepHealth record) {
        UserSleepHealth cached = sleepHealthRedisService.select(record.getUserId(), record.getDate());
        if (cached != null && cached.getId() != null) {
            log.info("睡眠记录缓存命中, userId: {}, date: {}", record.getUserId(), record.getDate());
            return true;
        }

        UserSleepHealth fromDb = sleepHealthMapper.selectByUserIdAndDate(record.getUserId(), record.getDate());
        if (fromDb != null && fromDb.getId() != null) {
            sleepHealthRedisService.save(fromDb);
            return true;
        }
        return false;
    }
}
