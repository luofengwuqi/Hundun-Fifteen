package com.createpen.fifteenhundun.module.sleep.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SleepHealthDTO {
    /** 睡眠质量：0差 1一般 2好 */
    private Integer quality;
    /** 睡眠时长（分钟） */
    private Integer durationMin;
}
