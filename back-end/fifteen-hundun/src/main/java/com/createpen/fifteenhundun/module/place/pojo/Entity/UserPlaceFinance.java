package com.createpen.fifteenhundun.module.place.pojo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPlaceFinance implements Serializable {
    private Long id;
    private Long userId;
    private LocalDate date;
    private Integer transportCost;
    private String transportMode;
    private String location;
    private LocalDateTime createdAt;
}
