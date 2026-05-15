package com.createpen.fifteenhundun.module.selecter.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceVo;
import com.createpen.fifteenhundun.module.selecter.service.FinanceDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/finance")
@RestController
@Slf4j
//聚合模块
public class FinanceDataController {
    private final FinanceDataService financeDataService;

    public FinanceDataController(FinanceDataService financeDataService) {
        this.financeDataService = financeDataService;
    }

    @GetMapping
    public Result<List<FinanceVo>> getSingleFinanceData() {
        log.info("获取财务模块简单数据表");
        return financeDataService.getSingleFinanceData();
    }
    @GetMapping("/{date}")
    public Result<List<FinanceDetailVo>> getFinanceData(@PathVariable LocalDate date) {
        log.info("获取财务模块详细数据表");
        return financeDataService.getFinanceData(date);
    }
}
