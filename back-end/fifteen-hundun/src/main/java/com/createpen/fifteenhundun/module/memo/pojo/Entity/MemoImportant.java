package com.createpen.fifteenhundun.module.memo.pojo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoImportant implements Serializable {
    private Long id;
    private Long userId;
    private String text;
    private LocalDateTime createdAt;
}
