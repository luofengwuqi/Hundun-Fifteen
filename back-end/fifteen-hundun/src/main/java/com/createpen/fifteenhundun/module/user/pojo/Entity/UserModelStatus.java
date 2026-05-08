package com.createpen.fifteenhundun.module.user.pojo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModelStatus implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 绑定用户ID（逻辑关联users表）
     */
    private Long userId;

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

    /**
     * 是否新用户 0否 1是
     */
    private Integer isNewUser;



}
