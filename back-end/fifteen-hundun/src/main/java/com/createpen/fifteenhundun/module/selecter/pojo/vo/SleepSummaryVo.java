package com.createpen.fifteenhundun.module.selecter.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SleepSummaryVo {
    private double avgDuration;
    private double avgQuality;
    private Map<Integer, Integer> distribution;
    private int totalDays;
    private List<SleepEntry> entries;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class SleepEntry {
        private LocalDate date;
        private Integer quality;
        private Integer durationMin;
    }
}
