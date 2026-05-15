package com.createpen.fifteenhundun.module.selecter.provider;

import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceVo;

import java.time.LocalDate;

public interface FinanceProvider {
    FinanceDetailVo getFinanceDetailData(LocalDate date);

    FinanceVo getFinanceSingleData();
}
