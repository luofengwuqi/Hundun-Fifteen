package com.createpen.fifteenhundun.module.food.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayFoodDTO {
    private Integer morningFood;
    private Integer afternoonFood;
    private Integer eveningFood;
    private Integer elseFood;
}
