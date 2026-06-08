package com.createpen.fifteenhundun.module.music.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("music")
public class Music implements Serializable {
    private Long id;
    private String title;
    private Integer duration;
    private String audioUrl;
    private String coverUrl;
    private String uploaderName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
