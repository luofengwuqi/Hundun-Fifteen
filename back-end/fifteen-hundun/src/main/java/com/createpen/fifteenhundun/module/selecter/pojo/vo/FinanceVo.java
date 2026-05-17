package com.createpen.fifteenhundun.module.selecter.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FinanceVo {
    private LocalDate date;//当前日期
    private Integer totalPrice;//总共的钱数
}
