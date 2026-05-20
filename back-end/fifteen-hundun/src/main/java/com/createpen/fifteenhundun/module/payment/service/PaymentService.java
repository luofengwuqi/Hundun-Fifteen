package com.createpen.fifteenhundun.module.payment.service;

import com.createpen.fifteenhundun.module.payment.pojo.dto.PaymentDto;

import java.util.List;

public interface PaymentService {
    void savePayment(List<PaymentDto> dto);
}
