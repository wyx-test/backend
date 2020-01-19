package com.ztesoft.config.compare.dto;

import lombok.Data;


@Data
public class ContentValueInfo {
    private String name;
    //    1 same, 2 diff, 3. lack 4 additional
    private Integer status;
    private String method;
    private String sourceValue;
    private String targetValue;
    private String sectionName;
}
