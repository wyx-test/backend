package com.ztesoft.config.compare.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class FileCollect {
    @Id
    @GeneratedValue
    private Long id;
    private String fileName;
    private Long hostId;
    private Long fileId;
    private Long projectId;
    private String localPath;
    private String remotePath;
    private Integer type;
    //    success 0, failed -1
    private Integer status;
    private String errMsg;
    private Boolean master;
}
