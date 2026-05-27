package com.createpen.fifteenhundun.module.mood.pojo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMoodHealth implements Serializable {
    private Long id;
    private Long userId;
    private LocalDate date;
    private Integer mood;
    private String note;
    private LocalDateTime createdAt;
}
