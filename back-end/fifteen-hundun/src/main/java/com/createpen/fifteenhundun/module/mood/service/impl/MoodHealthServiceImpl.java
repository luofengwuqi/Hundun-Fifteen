package com.createpen.fifteenhundun.module.mood.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.exception.MyException.UserExistedException;
import com.createpen.fifteenhundun.module.mood.cache.MoodHealthRedisService;
import com.createpen.fifteenhundun.module.mood.mapper.MoodHealthMapper;
import com.createpen.fifteenhundun.module.mood.pojo.DTO.MoodHealthDTO;
import com.createpen.fifteenhundun.module.mood.pojo.Entity.UserMoodHealth;
import com.createpen.fifteenhundun.module.mood.service.MoodHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
public class MoodHealthServiceImpl implements MoodHealthService {

    private final MoodHealthRedisService moodHealthRedisService;
    private final MoodHealthMapper moodHealthMapper;

    public MoodHealthServiceImpl(MoodHealthRedisService moodHealthRedisService,
                                 MoodHealthMapper moodHealthMapper) {
        this.moodHealthRedisService = moodHealthRedisService;
        this.moodHealthMapper = moodHealthMapper;
    }

    @Transactional
    @Override
    public int insertMood(MoodHealthDTO dto) {
        UserMoodHealth record = dtoToEntity(dto);
        validateRecord(record);

        if (isMoodExist(record)) {
            log.info("心情记录已存在，跳过插入, userId: {}, date: {}", record.getUserId(), record.getDate());
            return 0;
        }

        moodHealthMapper.insertMood(record);
        moodHealthRedisService.save(record);
        log.info("心情记录已录入, userId: {}, date: {}", record.getUserId(), record.getDate());
        return 1;
    }

    @Transactional
    @Override
    public void updateMood(MoodHealthDTO dto) {
        UserMoodHealth record = dtoToEntity(dto);
        validateRecord(record);

        if (!isMoodExist(record)) {
            throw new UserExistedException("心情记录不存在，无法更新");
        }

        moodHealthMapper.updateMood(record);
        moodHealthRedisService.delete(record.getUserId(), record.getDate());
        log.info("心情记录已更新, userId: {}, date: {}", record.getUserId(), record.getDate());
    }

    private UserMoodHealth dtoToEntity(MoodHealthDTO dto) {
        UserMoodHealth record = new UserMoodHealth();
        record.setUserId(BaseContext.getCurrentId());
        record.setDate(LocalDate.now());
        record.setMood(dto.getMood());
        record.setNote(dto.getNote());
        return record;
    }

    private void validateRecord(UserMoodHealth record) {
        if (record.getMood() == null || record.getMood() < 0 || record.getMood() > 4) {
            throw new IllegalArgumentException("心情等级须为 0~4");
        }
        String note = record.getNote();
        if (note != null && note.length() > 500) {
            throw new IllegalArgumentException("心情备注不能超过 500 字");
        }
    }

    private boolean isMoodExist(UserMoodHealth record) {
        UserMoodHealth cached = moodHealthRedisService.select(record.getUserId(), record.getDate());
        if (cached != null && cached.getId() != null) {
            log.info("心情记录缓存命中, userId: {}, date: {}", record.getUserId(), record.getDate());
            return true;
        }

        UserMoodHealth fromDb = moodHealthMapper.selectByUserIdAndDate(record.getUserId(), record.getDate());
        if (fromDb != null && fromDb.getId() != null) {
            moodHealthRedisService.save(fromDb);
            return true;
        }
        return false;
    }
}
