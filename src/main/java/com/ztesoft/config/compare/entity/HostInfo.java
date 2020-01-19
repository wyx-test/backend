package com.ztesoft.config.compare.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
public class HostInfo {
    @Id
    @GeneratedValue
    private Long hostId;
    private String hostIp;
    private Long projectId;
    private Integer masterFlag;
    private Integer port;
    private String user;
    private String password;
    private String additionValue;
    private String homeDir;
}
