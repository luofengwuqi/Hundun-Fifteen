package com.createpen.fifteenhundun.module.place.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceHealthDTO {
    private String location;
    private String transportMode;
    private Integer steps;
}
