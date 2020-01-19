package com.ztesoft.config.compare.service;

import com.ztesoft.config.compare.entity.FileInfo;

import java.util.List;
import java.util.Map;

public interface FileInfoService {
    Map<String,Object> insert(FileInfo fileInfo);

    Map<String,Object> update(FileInfo fileInfo);

    Map<String,Object> delete(Long fileId);

    boolean deleteByProjectId(Long projectId);

    List<FileInfo> findAll();

    List<FileInfo> findByProjectId(Long projectId);
}
