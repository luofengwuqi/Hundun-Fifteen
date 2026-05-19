package com.createpen.fifteenhundun.module.selecter.provider;

import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;

import java.time.LocalDate;
import java.util.Map;

//这个方法约束了和财务相关的表必须有这些方法 方便聚合 方便整理多表的数据
public interface FinanceProvider {
    FinanceDetailVo getFinanceDetailData(LocalDate date);

    //LocalDate:date Integer:totalPrice(当日总花费)
    Map<LocalDate,Integer> getFinanceSimpleData();
}
