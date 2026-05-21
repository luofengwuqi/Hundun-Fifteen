package com.createpen.fifteenhundun.module.selecter.service.impl;

import com.createpen.fifteenhundun.common.context.BaseContext;
import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceDetailVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.FinanceVo;
import com.createpen.fifteenhundun.module.selecter.provider.FinanceProvider;
import com.createpen.fifteenhundun.module.selecter.service.FinanceDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class FinanceDataServiceImpl implements FinanceDataService {

    private final List<FinanceProvider> providers;
    private final Executor financeExecutor;  // 用 Spring 的线程池，不要自己 new Thread

    public FinanceDataServiceImpl(
            List<FinanceProvider> providers,
            @Qualifier("generalExecutor") Executor financeExecutor
    ) {
        this.providers = providers;
        this.financeExecutor = financeExecutor;
    }



    @Override
    public Result<List<FinanceVo>> getSingleFinanceData() {
        Long userId = BaseContext.getCurrentId();
        List<CompletableFuture<Map<LocalDate, Integer>>> futures = new ArrayList<>();
        //1.执行异步
        for (FinanceProvider provider : providers){
            //异步任务<任务返回值类型>
            CompletableFuture<Map<LocalDate, Integer>> future =
                    //开启异步任务
                    CompletableFuture.supplyAsync(()->{
                        try{
                            BaseContext.setCurrentId(userId);//给子线程设置当前用户 ID
                            return provider.getFinanceSimpleData();
                        } finally {
                            BaseContext.clearAll();//清理线程局部变量，防止内存泄漏、数据错乱
                        }
                    },financeExecutor);// 指定线程池
            futures.add(future);
        }
        //2.等待异步
        CompletableFuture<?>[] arr = futures.toArray(new CompletableFuture[0]);//传参:空数组模版
        CompletableFuture.allOf(arr).join();
        // .allOf(数组)
        // 作用：等待【数组中所有的异步任务】全部执行完成
        // .join()
        // 作用：让主线程【阻塞在这里】，不继续往下走 直到：所有子线程（查food、payment、travel...）全部查完，才放行

        // 3.处理结果
        Map<LocalDate, Integer> totalMap = new LinkedHashMap<>();
        for (CompletableFuture<Map<LocalDate, Integer>> future : futures) {
            Map<LocalDate, Integer> singleMap = future.join();  // 取这个异步任务的结果
            if (singleMap != null && !singleMap.isEmpty()) {
                for (Map.Entry<LocalDate, Integer> entry : singleMap.entrySet()) {
                    LocalDate date = entry.getKey();
                    Integer amount = entry.getValue();
                    // 同一天累加
                    totalMap.merge(date, amount, Integer::sum);
                }
            }
        }
        List<FinanceVo> voList = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : totalMap.entrySet()) {
            FinanceVo vo = new FinanceVo();
            vo.setDate(entry.getKey());
            vo.setTotalPrice(entry.getValue());
            voList.add(vo);
        }
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
