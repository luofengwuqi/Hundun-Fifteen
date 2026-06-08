package com.createpen.fifteenhundun.module.music.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicDTO implements Serializable {
    private Long id;
    private String title;
    private Integer duration;
    private String coverUrl;
    private String audioUrl;
    private String uploaderName;
}
