package com.createpen.fifteenhundun.module.payment.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentDto {
    private String name;
    private Integer price;
    private Integer type;
}
