package com.createpen.fifteenhundun.module.food.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayFoodDTO {
    private String morningFood;
    private String afternoonFood;
    private String eveningFood;
    private String elseFood;
}
