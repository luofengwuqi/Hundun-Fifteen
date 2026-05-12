package com.createpen.fifteenhundun.module.food.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DayFoodPriceDTO {
    private BigDecimal morningFoodPrice;
    private BigDecimal afternoonFoodPrice;
    private BigDecimal eveningFoodPrice;
    private BigDecimal elseFoodPrice;
}
