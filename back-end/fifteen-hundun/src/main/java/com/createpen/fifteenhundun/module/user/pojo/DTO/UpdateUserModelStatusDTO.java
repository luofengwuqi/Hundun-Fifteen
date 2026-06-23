package com.createpen.fifteenhundun.module.user.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserModelStatusDTO {
    /**
     * 健康模块 0禁用 1启用
     */
    private Integer health;

    /**
     * 财务模块 0禁用 1启用
     */
    private Integer finance;

    /**
     * 知识模块 0禁用 1启用
     */
    private Integer knowledge;

    /**
     * 备忘模块 0禁用 1启用
     */
    private Integer memo;

    private Integer music;

    /**
     * 是否新用户 0否 1是
     */
    private Integer isNewUser;

    private Long userId;
}
