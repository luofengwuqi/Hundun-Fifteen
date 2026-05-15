package com.createpen.fifteenhundun.module.selecter.service.impl;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceVo;
import com.createpen.fifteenhundun.module.selecter.provider.FinanceProvider;
import com.createpen.fifteenhundun.module.selecter.service.FinanceDataService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinanceDataServiceImpl implements FinanceDataService {
    private final List<FinanceProvider> providers;

    public FinanceDataServiceImpl(List<FinanceProvider> providers) {
        this.providers = providers;
    }


    @Override
    public Result<List<FinanceVo>> getSingleFinanceData() {
        return null;
    }

    @Override
    public Result<List<FinanceDetailVo>> getFinanceData(LocalDate date) {
        List<FinanceDetailVo> list = new ArrayList<>();
        for (FinanceProvider provider : providers) {
            FinanceDetailVo financeData = provider.getFinanceData(date);
            list.add(financeData);
        }
        return Result.success(list);
    }
}
