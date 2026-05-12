
package com.createpen.fifteenhundun.module.food.pojo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayFoodPrice implements Serializable {
    private Long id;
    private Long userId;
    private LocalDate date;
    private Integer morningFoodPrice;
    private Integer afternoonFoodPrice;
    private Integer eveningFoodPrice;
    private Integer elseFoodPrice;
}