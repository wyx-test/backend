package com.ztesoft.config.compare.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class FileInfo {
    @Id
    @GeneratedValue
    private Long fileId;
    private Long projectId;
    private String fileName;
    // 1,property 2.ini 0.others
    private Integer type;
    private String generalPath;
    private String comments;
    private String specialValue;
    private String deltaFileInfos;
}
