package com.ztesoft.config.compare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpecialValue {
    private String sectionName;
    private String key;
//    1.ignore 2.existed 3.notExisted 4.special value
    private Integer method;
    private String value;
}
