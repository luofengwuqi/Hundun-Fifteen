package com.createpen.fifteenhundun.module.selecter.service;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceVo;

import java.time.LocalDate;
import java.util.List;

public interface FinanceDataService {

    Result<List<FinanceVo>> getSingleFinanceData();

    Result<List<FinanceDetailVo>> getFinanceData(LocalDate date);
}
