package com.createpen.fifteenhundun.module.ai.diary.service;

import com.createpen.fifteenhundun.module.ai.diary.pojo.DTO.DiaryConfirmRequest;
import com.createpen.fifteenhundun.module.food.service.FoodFinanceService;
import com.createpen.fifteenhundun.module.food.service.FoodHealthService;
import com.createpen.fifteenhundun.module.memo.service.MemoCommonService;
import com.createpen.fifteenhundun.module.memo.service.MemoImportantService;
import com.createpen.fifteenhundun.module.mood.service.MoodHealthService;
import com.createpen.fifteenhundun.module.payment.service.PaymentService;
import com.createpen.fifteenhundun.module.place.service.PlaceFinanceService;
import com.createpen.fifteenhundun.module.place.service.PlaceHealthService;
import com.createpen.fifteenhundun.module.sleep.service.SleepHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class DiaryExecutionService {

    /**
     * 判断 DTO 对象的所有字段是否都为空（null / 空字符串）
     * 避免前端传了空结构体仍然写入数据库
     */
    private boolean isEmpty(Object dto) {
        if (dto == null) return true;
        for (var field : dto.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object val = field.get(dto);
                if (val instanceof String s) {
                    if (!s.isEmpty()) return false;
                } else if (val != null) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                return false;
            }
        }
        return true;
    }

    private final FoodHealthService foodHealthService;
    private final FoodFinanceService foodFinanceService;
    private final SleepHealthService sleepHealthService;
    private final MoodHealthService moodHealthService;
    private final PlaceHealthService placeHealthService;
    private final PlaceFinanceService placeFinanceService;
    private final PaymentService paymentService;
    private final MemoCommonService memoCommonService;
    private final MemoImportantService memoImportantService;

    public DiaryExecutionService(FoodHealthService foodHealthService,
                                  FoodFinanceService foodFinanceService,
                                  SleepHealthService sleepHealthService,
                                  MoodHealthService moodHealthService,
                                  PlaceHealthService placeHealthService,
                                  PlaceFinanceService placeFinanceService,
                                  PaymentService paymentService,
                                  MemoCommonService memoCommonService,
                                  MemoImportantService memoImportantService) {
        this.foodHealthService = foodHealthService;
        this.foodFinanceService = foodFinanceService;
        this.sleepHealthService = sleepHealthService;
        this.moodHealthService = moodHealthService;
        this.placeHealthService = placeHealthService;
        this.placeFinanceService = placeFinanceService;
        this.paymentService = paymentService;
        this.memoCommonService = memoCommonService;
        this.memoImportantService = memoImportantService;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> execute(DiaryConfirmRequest request) {
        Map<String, Object> results = new LinkedHashMap<>();

        if (!isEmpty(request.getFoodHealth())) {
            try {
                int code = foodHealthService.insertFood(request.getFoodHealth());
                results.put("foodHealth", code == 1 ? "录入成功" : "今日已有记录，跳过");
            } catch (Exception e) {
                log.error("饮食健康录入失败", e);
                results.put("foodHealth", "录入失败: " + e.getMessage());
                throw e;
            }
        }

        if (!isEmpty(request.getFoodFinance())) {
            try {
                Integer code = foodFinanceService.insertFoodMoney(request.getFoodFinance());
                results.put("foodFinance", code == 1 ? "录入成功" : "今日已有记录，跳过");
            } catch (Exception e) {
                log.error("餐饮花费录入失败", e);
                results.put("foodFinance", "录入失败: " + e.getMessage());
                throw e;
            }
        }

        if (!isEmpty(request.getSleepHealth())) {
            try {
                int code = sleepHealthService.insertSleep(request.getSleepHealth());
                results.put("sleepHealth", code == 1 ? "录入成功" : "今日已有记录，跳过");
            } catch (Exception e) {
                log.error("睡眠记录录入失败", e);
                results.put("sleepHealth", "录入失败: " + e.getMessage());
                throw e;
            }
        }

        if (!isEmpty(request.getMoodHealth())) {
            try {
                int code = moodHealthService.insertMood(request.getMoodHealth());
                results.put("moodHealth", code == 1 ? "录入成功" : "今日已有记录，跳过");
            } catch (Exception e) {
                log.error("心情记录录入失败", e);
                results.put("moodHealth", "录入失败: " + e.getMessage());
                throw e;
            }
        }

        if (!isEmpty(request.getPlaceHealth())) {
            try {
                int code = placeHealthService.insertPlace(request.getPlaceHealth());
                results.put("placeHealth", code == 1 ? "录入成功" : "今日已有记录，跳过");
            } catch (Exception e) {
                log.error("外出活动录入失败", e);
                results.put("placeHealth", "录入失败: " + e.getMessage());
                throw e;
            }
        }

        if (!isEmpty(request.getPlaceFinance())) {
            try {
                int code = placeFinanceService.insertPlaceFinance(request.getPlaceFinance());
                results.put("placeFinance", code == 1 ? "录入成功" : "今日已有记录，跳过");
            } catch (Exception e) {
                log.error("交通花费录入失败", e);
                results.put("placeFinance", "录入失败: " + e.getMessage());
                throw e;
            }
        }

        if (request.getPayments() != null && !request.getPayments().isEmpty()) {
            try {
                paymentService.savePayment(request.getPayments());
                results.put("payments", "录入成功（共" + request.getPayments().size() + "条）");
            } catch (Exception e) {
                log.error("消费记录录入失败", e);
                results.put("payments", "录入失败: " + e.getMessage());
                throw e;
            }
        }

        if (request.getMemoCommons() != null && !request.getMemoCommons().isEmpty()) {
            try {
                for (var memo : request.getMemoCommons()) {
                    memoCommonService.insertMemoCommon(memo);
                }
                results.put("memoCommons", "录入成功（共" + request.getMemoCommons().size() + "条）");
            } catch (Exception e) {
                log.error("普通备忘录入失败", e);
                results.put("memoCommons", "录入失败: " + e.getMessage());
                throw e;
            }
        }

        if (request.getMemoImportants() != null && !request.getMemoImportants().isEmpty()) {
            try {
                for (var memo : request.getMemoImportants()) {
                    memoImportantService.insertMemoImportant(memo);
                }
                results.put("memoImportants", "录入成功（共" + request.getMemoImportants().size() + "条）");
            } catch (Exception e) {
                log.error("重要备忘录入失败", e);
                results.put("memoImportants", "录入失败: " + e.getMessage());
                throw e;
            }
        }

        return results;
    }
}
