package com.createpen.fifteenhundun.module.food.pojo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayFood implements Serializable {
    private Long id;
    private Long userId;
    private LocalDate date;
    private String morningFood;
    private String afternoonFood;
    private String eveningFood;
    private String elseFood;
}