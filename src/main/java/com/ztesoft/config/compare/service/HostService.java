package com.ztesoft.config.compare.service;

import com.ztesoft.config.compare.dto.HostInfoDto;
import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostDetail;
import com.ztesoft.config.compare.entity.HostInfo;

import java.util.List;
import java.util.Map;

/**
 * host服务接口
 */
public interface HostService {

    Map<String, Object> insert(HostInfoDto hostInfoDto);

    Map<String, Object> update(HostInfoDto hostInfoDto);

    List<HostInfoDto> findAll();

    List<HostInfoDto> findByProjectId(Long id);

    HostInfoDto findMasterByProjectId(Long id);

    List<HostInfoDto> findSlaveByProjectId(Long id);

    Map<String, Object> delete(Long hostId);

    List<HostDetail> getHostDetailListById(Long projectId);
}
