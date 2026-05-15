package com.createpen.fifteenhundun.module.selecter.provider;

import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;

import java.time.LocalDate;

public interface FinanceProvider {
    FinanceDetailVo getFinanceData(LocalDate date);
}
