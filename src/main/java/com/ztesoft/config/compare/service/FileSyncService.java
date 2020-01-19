package com.ztesoft.config.compare.service;

import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostInfo;
import com.ztesoft.config.compare.entity.Project;

import java.util.List;
import java.util.Map;

/**
 * 文件服务接口
 */
//@Service
public interface FileSyncService {

    Map<String, Object> syncSingleFile(Long fileId);
}
