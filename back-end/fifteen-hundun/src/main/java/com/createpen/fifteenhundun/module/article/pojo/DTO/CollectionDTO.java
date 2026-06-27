package com.createpen.fifteenhundun.module.article.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CollectionDTO类，用于数据传输对象
 * 使用了Lombok注解简化代码
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CollectionDTO {
    private Integer id;
    private Integer userId;
    private Integer articleId ;
}
