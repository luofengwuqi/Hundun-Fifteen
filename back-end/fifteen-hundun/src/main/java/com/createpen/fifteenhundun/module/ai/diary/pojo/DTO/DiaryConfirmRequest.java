package com.createpen.fifteenhundun.module.ai.diary.pojo.DTO;

import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodDTO;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodPriceDTO;
import com.createpen.fifteenhundun.module.memo.pojo.DTO.MemoCommonDTO;
import com.createpen.fifteenhundun.module.memo.pojo.DTO.MemoImportantDTO;
import com.createpen.fifteenhundun.module.mood.pojo.DTO.MoodHealthDTO;
import com.createpen.fifteenhundun.module.payment.pojo.dto.PaymentDto;
import com.createpen.fifteenhundun.module.place.pojo.DTO.PlaceFinanceDTO;
import com.createpen.fifteenhundun.module.place.pojo.DTO.PlaceHealthDTO;
import com.createpen.fifteenhundun.module.sleep.pojo.DTO.SleepHealthDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryConfirmRequest {
    private String date;
    private DayFoodDTO foodHealth;
    private DayFoodPriceDTO foodFinance;
    private SleepHealthDTO sleepHealth;
    private MoodHealthDTO moodHealth;
    private PlaceHealthDTO placeHealth;
    private PlaceFinanceDTO placeFinance;
    private List<PaymentDto> payments;
    private List<MemoCommonDTO> memoCommons;
    private List<MemoImportantDTO> memoImportants;
}
