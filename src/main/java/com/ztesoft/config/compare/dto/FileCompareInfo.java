package com.ztesoft.config.compare.dto;

import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostInfo;
import lombok.Data;

import java.util.Map;

/**
 * @author wuyaxiong
 */
@Data
public class FileCompareInfo {
    private Long id;
    private String fileName;
    private String source;
    private String target;
    private Integer type;
    private HostInfo hostInfo;
    private FileInfo fileInfo;
    private String specialValueStr;
    private Map<String,String> hostMap;
    private String charsetName;
}
