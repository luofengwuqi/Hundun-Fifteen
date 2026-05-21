package com.createpen.fifteenhundun.common.util;

import java.math.BigDecimal;

public class FinanceUtil {
public static Integer BigDecimalX100(BigDecimal num){
    if (num == null) {
        return 0; // 或者 throw new IllegalArgumentException("参数不能为null");
    }
    return num.multiply(BigDecimal.valueOf(100)).intValue();
}

}
