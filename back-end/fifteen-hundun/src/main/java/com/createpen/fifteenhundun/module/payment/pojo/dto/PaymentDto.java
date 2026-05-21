package com.createpen.fifteenhundun.module.payment.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentDto {
    private String name;
    private BigDecimal price;
    private Integer type;
}
