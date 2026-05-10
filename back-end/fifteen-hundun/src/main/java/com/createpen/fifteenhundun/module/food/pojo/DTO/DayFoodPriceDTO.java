package com.createpen.fifteenhundun.module.food.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DayFoodPriceDTO {
    private Integer morningGoodFoodPrice;
    private Integer afternoonFoodPrice;
    private Integer eveningFoodPrice;
    private Integer elseFoodPrice;
}
