
package com.createpen.fifteenhundun.module.food.pojo.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor

@TableName("user_food_finance")
public class DayFoodPrice implements Serializable {
    private Long id;
    private Long userId;
    private LocalDate date;
    private Integer morningFoodPrice;
    private Integer afternoonFoodPrice;
    private Integer eveningFoodPrice;
    private Integer elseFoodPrice;
    private Integer totalPrice;
}