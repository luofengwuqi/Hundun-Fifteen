package com.createpen.fifteenhundun.module.place.service;

import com.createpen.fifteenhundun.module.place.pojo.DTO.PlaceFinanceDTO;

public interface PlaceFinanceService {
    int insertPlaceFinance(PlaceFinanceDTO dto);
    void updatePlaceFinance(PlaceFinanceDTO dto);
}
