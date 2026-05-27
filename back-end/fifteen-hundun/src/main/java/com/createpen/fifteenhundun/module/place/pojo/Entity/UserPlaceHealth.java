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
public class UserPlaceHealth implements Serializable {
    private Long id;
    private Long userId;
    private LocalDate date;
    private String location;
    private String transportMode;
    private Integer steps;
    private LocalDateTime createdAt;
}
