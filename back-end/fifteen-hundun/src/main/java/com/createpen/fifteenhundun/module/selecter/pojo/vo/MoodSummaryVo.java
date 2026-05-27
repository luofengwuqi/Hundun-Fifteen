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
public class MoodSummaryVo {
    private double average;
    private Map<Integer, Integer> distribution;
    private int totalDays;
    private List<MoodEntry> entries;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class MoodEntry {
        private LocalDate date;
        private Integer mood;
        private String note;
    }
}
