package com.createpen.fifteenhundun.module.memo.pojo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoCommon implements Serializable {
    private Long id;
    private Long userId;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private String text;
    private Integer status; // 0:关闭邮件通知, 1:开启邮件通知
    private String email;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
