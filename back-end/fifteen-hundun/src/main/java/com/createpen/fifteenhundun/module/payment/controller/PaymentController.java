package com.createpen.fifteenhundun.module.payment.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.payment.pojo.dto.PaymentDto;
import com.createpen.fifteenhundun.module.payment.pojo.entity.Payment;
import com.createpen.fifteenhundun.module.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @Autowired
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    //获取paymentList
    @PostMapping
    public Result<Payment> savePayment(@RequestBody PaymentDto dto) {
        log.info("post payments");
        paymentService.savePayment(dto);
        return Result.success();
    }
}
