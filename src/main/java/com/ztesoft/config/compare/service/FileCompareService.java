package com.ztesoft.config.compare.service;

import com.ztesoft.config.compare.dto.CompareReport;
import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostInfo;
import com.ztesoft.config.compare.entity.Project;

import java.util.List;
import java.util.Map;

/**
 * 文件服务接口
 */
public interface FileCompareService {

    Map<String, Object> syncFile2Server(Long projectId);

    Map<String, Object> syncFile2ServerByHostId(Long hostId);

    Map<String, Object> compareFile(Project project,HostInfo hostInfo, List<FileInfo> fileInfos);

    Map<String, Object> collectFileByProjectId(Long projectId);

    Map<String,Object> compareFileByProject(Long projectId);

    Map<String, Object> compareFileByFileCollect(Long id);

    List<CompareReport> getReportFromCompareResult(Map<String, Map<String, Object>> compareResult);
}
