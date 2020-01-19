package com.ztesoft.config.compare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeltaFileInfo {
    private Long hostId;
    private String path;
    private boolean isRelativePath;
}
