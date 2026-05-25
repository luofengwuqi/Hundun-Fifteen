package com.createpen.fifteenhundun.module.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.createpen.fifteenhundun.module.payment.pojo.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
    List<Payment> getFinanceSimpleData(Long userId);

    void insertPaymentList(List<Payment> payments);
}
