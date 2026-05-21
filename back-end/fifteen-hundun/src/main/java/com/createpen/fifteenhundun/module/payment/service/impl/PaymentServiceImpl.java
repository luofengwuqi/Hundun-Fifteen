package com.createpen.fifteenhundun.module.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.util.FinanceUtil;
import com.createpen.fifteenhundun.module.payment.mapper.PaymentMapper;
import com.createpen.fifteenhundun.module.payment.pojo.dto.PaymentDto;
import com.createpen.fifteenhundun.module.payment.pojo.entity.Payment;
import com.createpen.fifteenhundun.module.payment.service.PaymentService;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;
import com.createpen.fifteenhundun.module.selecter.provider.FinanceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.createpen.fifteenhundun.common.constant.finance.FinanceDetailConstant.FINANCE_TITLE_PAYMENT;
import static com.createpen.fifteenhundun.common.constant.finance.PaymentTypeConstant.PAYMENT_TYPE_UNKNOWN;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService,  FinanceProvider {
    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }

    /*
        FinanceProvider
    */
    @Override
    public FinanceDetailVo getFinanceDetailData(LocalDate date) {
        //查询数据库
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Payment::getId,Payment::getName,Payment::getPrice)
                .eq(Payment::getUserId,currentId)
                .eq(Payment::getDate,date);
        List<Payment> paymentList = paymentMapper.selectList(wrapper);
        //封装对象
        FinanceDetailVo vo = new FinanceDetailVo();
        vo.setTitle(FINANCE_TITLE_PAYMENT);
        vo.setDataMap(paymentListToDetailMap(paymentList));
        return vo;
    }
    //辅助方法
    private Map<String,Integer> paymentListToDetailMap(List<Payment> paymentList){
        Map<String,Integer> map = new LinkedHashMap<>();
        for(Payment payment:paymentList){
            String key = payment.getName();
            Integer value = payment.getPrice();
            //校验是否有一样的 key
            if(map.containsKey(key)){
                key = key+"_"+payment.getId();
            }
            map.put(key,value);
        }
        return map;
    }

    @Override
    public Map<LocalDate, Integer> getFinanceSimpleData() {
        //sql
        Long currentId = BaseContext.getCurrentId();
        List<Payment> financeSimpleData = paymentMapper.getFinanceSimpleData(currentId);
        return paymentListToSimpleMap(financeSimpleData);
    }
    private Map<LocalDate, Integer> paymentListToSimpleMap(List<Payment> paymentList) {
        Map<LocalDate, Integer> map = new LinkedHashMap<>();
        for(Payment payment:paymentList){
            LocalDate date = payment.getDate();
            Integer price = payment.getPrice();
            map.put(date,price);
        }
        return map;
    }


    @Override
    public void savePayment(List<PaymentDto> dtoList) {
        List<Payment> payments = PaymentDtoListToPaymentList(dtoList);
        log.info("保存payment: {}",payments);
        paymentMapper.insertPaymentList(payments);
    }
    private List<Payment> PaymentDtoListToPaymentList(List<PaymentDto> dtoList){
        List<Payment> paymentList = new ArrayList<>();
        for(PaymentDto dto:dtoList){
            Payment tempEntity = new Payment();
            tempEntity.setName(dto.getName());
            tempEntity.setPrice(FinanceUtil.BigDecimalX100(dto.getPrice())) ;
            tempEntity.setType(dto.getType());
            if (tempEntity.getType()==null){
                tempEntity.setType(PAYMENT_TYPE_UNKNOWN);
            }
            tempEntity.setUserId(BaseContext.getCurrentId());
            tempEntity.setDate(LocalDate.now());
            paymentList.add(tempEntity);
        }
        return paymentList;
    }
}
