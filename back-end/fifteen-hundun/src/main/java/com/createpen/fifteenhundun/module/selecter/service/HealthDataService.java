package com.createpen.fifteenhundun.module.selecter.service;

import com.createpen.fifteenhundun.module.selecter.pojo.vo.MoodSummaryVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.SleepSummaryVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.StepsSummaryVo;

import java.time.LocalDate;

public interface HealthDataService {

    MoodSummaryVo getMoodSummary(LocalDate start, LocalDate end);

    SleepSummaryVo getSleepSummary(LocalDate start, LocalDate end);

    StepsSummaryVo getStepsSummary(LocalDate start, LocalDate end);
}
