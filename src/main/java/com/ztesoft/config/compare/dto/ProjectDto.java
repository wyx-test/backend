package com.ztesoft.config.compare.dto;

import com.ztesoft.config.compare.entity.HostInfo;
import lombok.Data;


import java.util.List;

@Data
public class ProjectDto {
    private Long projectId;
    private String name;
    private String comments;
    private List<HostInfo> hostInfoList;
}
