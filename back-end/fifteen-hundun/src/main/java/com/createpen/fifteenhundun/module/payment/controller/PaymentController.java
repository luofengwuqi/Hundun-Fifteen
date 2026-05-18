package com.createpen.fifteenhundun.module.payment.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.payment.pojo.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    //获取paymentList
    @GetMapping
    public Result<Payment> payment() {
        log.info("get payments");
        return Result.success();
    }
}
