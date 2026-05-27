package com.createpen.fifteenhundun.module.selecter.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StepsSummaryVo {
    private int totalSteps;
    private double avgDailySteps;
    private int totalDays;
    private List<StepsEntry> entries;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class StepsEntry {
        private LocalDate date;
        private Integer steps;
        private String location;
        private String transportMode;
    }
}
