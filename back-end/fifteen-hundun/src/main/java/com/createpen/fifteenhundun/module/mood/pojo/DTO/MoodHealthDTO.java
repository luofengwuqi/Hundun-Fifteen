package com.createpen.fifteenhundun.module.mood.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodHealthDTO {
    /** 心情等级：0很差 1差 2一般 3好 4很好 */
    private Integer mood;
    /** 心情备注 */
    private String note;
}
