package com.createpen.fifteenhundun.module.selecter.service.impl;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceVo;
import com.createpen.fifteenhundun.module.selecter.provider.FinanceProvider;
import com.createpen.fifteenhundun.module.selecter.service.FinanceDataService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceDataServiceImpl implements FinanceDataService {
    private final List<FinanceProvider> providers;

    public FinanceDataServiceImpl(List<FinanceProvider> providers) {
        this.providers = providers;
    }


    @Override
    public Result<List<FinanceVo>> getSingleFinanceData() {
        // 总Map：保持插入顺序 + 存储最终 日期-总花费
        Map<LocalDate, Integer> totalMap = new LinkedHashMap<>();

        // 遍历所有模块（food、buy、travel...）
        for (FinanceProvider provider : providers) {
            // 获取单个模块的日期花费
            Map<LocalDate, Integer> singleMap = provider.getFinanceSimpleData();
            if (singleMap != null && !singleMap.isEmpty()) {
                // 核心：合并到总Map，相同日期自动累加金额
                singleMap.forEach((date, amount) -> 
                    totalMap.merge(date, amount, Integer::sum)
                );
            }
        }

        // 转换为前端需要的 List<FinanceVo>
        List<FinanceVo> voList = new ArrayList<>();
        totalMap.forEach((date, total) -> {
            FinanceVo vo = new FinanceVo();
            vo.setDate(date);
            vo.setTotalPrice(total);
            voList.add(vo);
        });

        return Result.success(voList);
    }

    @Override
    public Result<List<FinanceDetailVo>> getFinanceData(LocalDate date) {
        List<FinanceDetailVo> list = new ArrayList<>();
        for (FinanceProvider provider : providers) {
            FinanceDetailVo financeData = provider.getFinanceDetailData(date);
            if (financeData != null && !financeData.getDataMap().isEmpty()){
                list.add(financeData);
            }
        }
        return Result.success(list);
    }
}
