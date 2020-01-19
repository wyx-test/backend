package com.ztesoft.config.compare.repository;

import com.ztesoft.config.compare.entity.HostInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostInfoRepository extends JpaRepository<HostInfo, Long> {
    List<HostInfo> findByProjectId(Long projectId);

    HostInfo findByProjectIdAndHostIpAndUserAndPort(Long projectId,String hostIp,String user,Integer port);

    HostInfo findByProjectIdAndHostIpAndUserAndPortAndHostIdNot(Long projectId,String hostIp,String user,Integer port,Long hostId);

    HostInfo findByProjectIdAndHostIp(Long projectId, String hostIp);

    HostInfo findByProjectIdAndHostIpAndHostIdNot(Long projectId, String hostIp, Long hostId);

    HostInfo findByProjectIdAndMasterFlag(Long projectId, Integer masterFlag);

    List<HostInfo> findByProjectIdAndMasterFlagNot(Long projectId, Integer masterFlag);

    HostInfo findByMasterFlag(Integer masterFlag);

    HostInfo findByProjectIdAndMasterFlagAndHostIdNot(Long projectId, Integer masterFlag, Long hostId);

    void deleteByProjectId(Long projectId);
}
