package com.createpen.fifteenhundun.framework.task;

import com.createpen.fifteenhundun.module.memo.service.MemoCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemoEmailSendTask {
    private final MemoCommonService memoCommonService;

    public MemoEmailSendTask(MemoCommonService memoCommonService) {
        this.memoCommonService = memoCommonService;
    }

    //每天执行一次
    //执行时机:每天早上八点
    @Scheduled(cron = "0 0 8 * * ?")
    public void executeTask() {
        System.out.println("开始执行task任务:发送邮件");
        memoCommonService.sendEmailToMemoUser();
        System.out.println("完成执行task任务:发送邮件");
    }
}
