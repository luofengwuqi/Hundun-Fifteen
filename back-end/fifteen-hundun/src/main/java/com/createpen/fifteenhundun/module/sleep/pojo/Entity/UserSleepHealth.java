package com.createpen.fifteenhundun.module.sleep.pojo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSleepHealth implements Serializable {
    private Long id;
    private Long userId;
    private LocalDate date;
    private Integer quality;
    private Integer durationMin;
    private LocalDateTime createdAt;
}
