package com.ztesoft.config.compare.dto;

import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostInfo;
import lombok.Data;

import java.util.Map;

/**
 * @author wuyaxiong
 */
@Data
public class FileSyncInfo {
    private Long id;
    private String fileName;
    private String source;
    private String target;
    private Integer type;
    private HostInfo hostInfo;
    private String specialValueStr;
    private String charsetName;
    private String remotePath;
}
