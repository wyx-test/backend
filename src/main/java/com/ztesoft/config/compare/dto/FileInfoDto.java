package com.ztesoft.config.compare.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FileInfoDto {
    private Long fileId;
    private Long projectId;
    private String fileName;
    private String generalPath;
    // 1,property 2.ini 0.others
    private Integer type;
    private List<SpecialValue> specialValues;
    private String comments;
    private List<DeltaFileInfo> deltaFileInfos;
}
