package com.createpen.fifteenhundun.module.selecter.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FinanceDetailVo {
    private String title;
    private Map<String,Integer> dataMap;
//|______________饮食(title)
//|			   |
//|			   |________早餐(key)    10元(value)
//|			   |________午餐(key)    10元(value)
//|			   |________晚餐(key)    10元(value)
//|			   |________其他(key)    10元(value)
}
