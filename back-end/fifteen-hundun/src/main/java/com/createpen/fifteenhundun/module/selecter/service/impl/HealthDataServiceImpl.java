package com.createpen.fifteenhundun.module.selecter.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.module.mood.mapper.MoodHealthMapper;
import com.createpen.fifteenhundun.module.mood.pojo.Entity.UserMoodHealth;
import com.createpen.fifteenhundun.module.place.mapper.PlaceHealthMapper;
import com.createpen.fifteenhundun.module.place.pojo.Entity.UserPlaceHealth;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.MoodSummaryVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.MoodSummaryVo.MoodEntry;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.SleepSummaryVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.SleepSummaryVo.SleepEntry;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.StepsSummaryVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.StepsSummaryVo.StepsEntry;
import com.createpen.fifteenhundun.module.selecter.service.HealthDataService;
import com.createpen.fifteenhundun.module.sleep.mapper.SleepHealthMapper;
import com.createpen.fifteenhundun.module.sleep.pojo.Entity.UserSleepHealth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HealthDataServiceImpl implements HealthDataService {

    private final MoodHealthMapper moodHealthMapper;
    private final SleepHealthMapper sleepHealthMapper;
    private final PlaceHealthMapper placeHealthMapper;

    public HealthDataServiceImpl(MoodHealthMapper moodHealthMapper,
                                 SleepHealthMapper sleepHealthMapper,
                                 PlaceHealthMapper placeHealthMapper) {
        this.moodHealthMapper = moodHealthMapper;
        this.sleepHealthMapper = sleepHealthMapper;
        this.placeHealthMapper = placeHealthMapper;
    }

    @Override
    public MoodSummaryVo getMoodSummary(LocalDate start, LocalDate end) {
        Long userId = BaseContext.getCurrentId();
        List<UserMoodHealth> all = moodHealthMapper.selectAllByUserId(userId);

        List<UserMoodHealth> filtered = filterByDateRange(all, start, end);

        if (filtered.isEmpty()) {
            MoodSummaryVo vo = new MoodSummaryVo();
            vo.setAverage(0);
            vo.setDistribution(new LinkedHashMap<>());
            vo.setTotalDays(0);
            vo.setEntries(new ArrayList<>());
            return vo;
        }

        double avg = filtered.stream()
                .mapToInt(UserMoodHealth::getMood)
                .average()
                .orElse(0);

        Map<Integer, Integer> dist = new LinkedHashMap<>();
        dist.put(0, 0);
        dist.put(1, 0);
        dist.put(2, 0);
        dist.put(3, 0);
        dist.put(4, 0);
        for (UserMoodHealth r : filtered) {
            dist.merge(r.getMood(), 1, Integer::sum);
        }

        List<MoodEntry> entries = filtered.stream()
                .map(r -> new MoodEntry(r.getDate(), r.getMood(), r.getNote()))
                .collect(Collectors.toList());

        MoodSummaryVo vo = new MoodSummaryVo();
        vo.setAverage(Math.round(avg * 10.0) / 10.0);
        vo.setDistribution(dist);
        vo.setTotalDays(filtered.size());
        vo.setEntries(entries);
        return vo;
    }

    @Override
    public SleepSummaryVo getSleepSummary(LocalDate start, LocalDate end) {
        Long userId = BaseContext.getCurrentId();
        List<UserSleepHealth> all = sleepHealthMapper.selectAllByUserId(userId);

        List<UserSleepHealth> filtered = filterByDateRange(all, start, end);

        if (filtered.isEmpty()) {
            SleepSummaryVo vo = new SleepSummaryVo();
            vo.setAvgDuration(0);
            vo.setAvgQuality(0);
            vo.setDistribution(new LinkedHashMap<>());
            vo.setTotalDays(0);
            vo.setEntries(new ArrayList<>());
            return vo;
        }

        double avgDur = filtered.stream()
                .filter(r -> r.getDurationMin() != null)
                .mapToInt(UserSleepHealth::getDurationMin)
                .average()
                .orElse(0);

        double avgQual = filtered.stream()
                .mapToInt(UserSleepHealth::getQuality)
                .average()
                .orElse(0);

        Map<Integer, Integer> dist = new LinkedHashMap<>();
        dist.put(0, 0);
        dist.put(1, 0);
        dist.put(2, 0);
        for (UserSleepHealth r : filtered) {
            dist.merge(r.getQuality(), 1, Integer::sum);
        }

        List<SleepEntry> entries = filtered.stream()
                .map(r -> new SleepEntry(r.getDate(), r.getQuality(), r.getDurationMin()))
                .collect(Collectors.toList());

        SleepSummaryVo vo = new SleepSummaryVo();
        vo.setAvgDuration(Math.round(avgDur * 10.0) / 10.0);
        vo.setAvgQuality(Math.round(avgQual * 10.0) / 10.0);
        vo.setDistribution(dist);
        vo.setTotalDays(filtered.size());
        vo.setEntries(entries);
        return vo;
    }

    @Override
    public StepsSummaryVo getStepsSummary(LocalDate start, LocalDate end) {
        Long userId = BaseContext.getCurrentId();
        List<UserPlaceHealth> all = placeHealthMapper.selectAllByUserId(userId);

        List<UserPlaceHealth> filtered = filterByDateRange(all, start, end);

        if (filtered.isEmpty()) {
            StepsSummaryVo vo = new StepsSummaryVo();
            vo.setTotalSteps(0);
            vo.setAvgDailySteps(0);
            vo.setTotalDays(0);
            vo.setEntries(new ArrayList<>());
            return vo;
        }

        int totalSteps = filtered.stream()
                .mapToInt(r -> r.getSteps() != null ? r.getSteps() : 0)
                .sum();

        double avgSteps = (double) totalSteps / filtered.size();

        List<StepsEntry> entries = filtered.stream()
                .map(r -> new StepsEntry(r.getDate(), r.getSteps(), r.getLocation(), r.getTransportMode()))
                .collect(Collectors.toList());

        StepsSummaryVo vo = new StepsSummaryVo();
        vo.setTotalSteps(totalSteps);
        vo.setAvgDailySteps(Math.round(avgSteps * 10.0) / 10.0);
        vo.setTotalDays(filtered.size());
        vo.setEntries(entries);
        return vo;
    }

    private <T> List<T> filterByDateRange(List<T> all, LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return all;
        }
        return all.stream().filter(item -> {
            LocalDate date = getDateFromEntity(item);
            if (date == null) return false;
            boolean afterStart = start == null || !date.isBefore(start);
            boolean beforeEnd = end == null || !date.isAfter(end);
            return afterStart && beforeEnd;
        }).collect(Collectors.toList());
    }

    private LocalDate getDateFromEntity(Object entity) {
        if (entity instanceof UserMoodHealth) return ((UserMoodHealth) entity).getDate();
        if (entity instanceof UserSleepHealth) return ((UserSleepHealth) entity).getDate();
        if (entity instanceof UserPlaceHealth) return ((UserPlaceHealth) entity).getDate();
        return null;
    }
}
