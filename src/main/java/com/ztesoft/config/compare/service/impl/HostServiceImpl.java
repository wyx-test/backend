package com.ztesoft.config.compare.service.impl;

import com.alibaba.fastjson.JSON;
import com.ztesoft.config.compare.dto.HostInfoDto;
import com.ztesoft.config.compare.entity.HostDetail;
import com.ztesoft.config.compare.entity.HostInfo;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.service.HostService;
import com.ztesoft.config.compare.utils.HostUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HostServiceImpl implements HostService {
    private static Logger logger = LoggerFactory.getLogger(HostServiceImpl.class);
    @Autowired
    private HostInfoRepository hostInfoRepository;

    @Override
    public Map<String, Object> insert(HostInfoDto hostInfoDto) {
        Map<String, Object> resultMap = new HashMap<>();
        //        检查ip地址是否重复
        if (hostInfoRepository.findByProjectIdAndHostIpAndUserAndPort(hostInfoDto.getProjectId(),
                hostInfoDto.getHostIp(),
                hostInfoDto.getUser(),
                hostInfoDto.getPort()) != null) {
            resultMap.put("resultCode", -1);
            resultMap.put("errMsg", " combination of [" + hostInfoDto.getHostIp() +
                     "," + hostInfoDto.getUser() + "," + hostInfoDto.getPort()+ "] is already exists.");
            return resultMap;
        }
//        避免重复添加基准主机
        boolean isMaster = hostInfoDto.getMasterFlag() == 1;
        if (isMaster && hostInfoRepository.findByProjectIdAndMasterFlag(hostInfoDto.getProjectId(), 1) != null) {
            resultMap.put("resultCode", -1);
            resultMap.put("errMsg", "The master server is already added, only slave server can be added.");
            return resultMap;
        }
        HostInfo hostInfo = new HostInfo();
        BeanUtils.copyProperties(hostInfoDto, hostInfo);
//        todo 临时注释掉服务器用户名密码校验
        if (!HostUtil.checkHost(hostInfo)) {
            resultMap.put("resultCode", -1);
            resultMap.put("errMsg", "server information is not correct, please try again.");
            return resultMap;
        }

        hostInfo.setPassword(HostUtil.encryptDES(hostInfoDto.getPassword()));
        List<HostDetail> hostDetails = hostInfoDto.getHostDetailList();
        if (hostDetails != null && hostDetails.size() > 0) {
            hostInfo.setAdditionValue(JSON.toJSONString(hostDetails));
        } else {
            hostInfo.setAdditionValue("");
        }
//        保存host信息
        resultMap.put("hostInfo", hostInfoRepository.save(hostInfo));
        resultMap.put("resultCode", 0);
        return resultMap;
    }

    @Override
    public Map<String, Object> update(HostInfoDto hostInfoDto) {
        Map<String, Object> resultMap = new HashMap<>();
        if (hostInfoRepository.findByProjectIdAndHostIpAndUserAndPortAndHostIdNot(
                hostInfoDto.getProjectId(),
                hostInfoDto.getHostIp(),
                hostInfoDto.getUser(),
                hostInfoDto.getPort(),
                hostInfoDto.getHostId()) != null) {
            resultMap.put("resultCode", -1);
            resultMap.put("errMsg", " combination of [" + hostInfoDto.getHostIp() +
                    "," + hostInfoDto.getUser() + "," + hostInfoDto.getPort()+ "] is already exists.");
            return resultMap;
        }
        boolean isMaster = hostInfoDto.getMasterFlag() == 1;
        if (isMaster && hostInfoRepository.findByProjectIdAndMasterFlagAndHostIdNot(hostInfoDto.getProjectId(),
                1, hostInfoDto.getHostId()) != null) {
            resultMap.put("resultCode", -1);
            resultMap.put("errMsg", "The master server is already existed, cannot change to master server.");
            return resultMap;
        }
        HostInfo hostInfo = new HostInfo();
        BeanUtils.copyProperties(hostInfoDto, hostInfo);
//        todo 临时注释掉服务器用户名密码校验
        if (!HostUtil.checkHost(hostInfo)) {
            resultMap.put("resultCode", -1);
            resultMap.put("errMsg", "server information is not correct, please try again.");
            return resultMap;
        }
        hostInfo.setPassword(HostUtil.encryptDES(hostInfoDto.getPassword()));
        List<HostDetail> hostDetails = hostInfoDto.getHostDetailList();
        if (hostDetails != null && hostDetails.size() > 0) {
            hostInfo.setAdditionValue(JSON.toJSONString(hostDetails));
        }
//        保存host信息
        resultMap.put("hostInfo", hostInfoRepository.save(hostInfo));
        resultMap.put("resultCode", 0);
        return resultMap;
    }

    @Override
    public List<HostInfoDto> findAll() {
        List<HostInfo> hostInfos = hostInfoRepository.findAll();
        List<HostInfoDto> hostInfoDtos = new ArrayList<>(hostInfos.size());
        for (HostInfo hostInfo : hostInfos) {
            hostInfoDtos.add(HostUtil.transEntity2Dto(hostInfo));
        }
        return hostInfoDtos;
    }

    @Override
    public List<HostInfoDto> findByProjectId(Long id) {
        List<HostInfo> hostInfos = hostInfoRepository.findByProjectId(id);
        List<HostInfoDto> hostInfoDtos = new ArrayList<>(hostInfos.size());
        for (HostInfo hostInfo : hostInfos) {
            hostInfoDtos.add(HostUtil.transEntity2Dto(hostInfo));
        }
        return hostInfoDtos;
    }

    @Override
    public HostInfoDto findMasterByProjectId(Long id) {
        HostInfo hostInfo = hostInfoRepository.findByProjectIdAndMasterFlag(id, 1);
        return HostUtil.transEntity2Dto(hostInfo);
    }

    @Override
    public List<HostInfoDto> findSlaveByProjectId(Long id) {
        List<HostInfo> hostInfos = hostInfoRepository.findByProjectIdAndMasterFlagNot(id, 1);
        List<HostInfoDto> hostInfoDtos = new ArrayList<>(hostInfos.size());
        for (HostInfo hostInfo : hostInfos) {
            hostInfoDtos.add(HostUtil.transEntity2Dto(hostInfo));
        }
        return hostInfoDtos;
    }

    @Override
    public Map<String, Object> delete(Long hostId) {
        Map<String, Object> map = new HashMap<>();
        hostInfoRepository.deleteById(hostId);
        boolean existed = hostInfoRepository.existsById(hostId);
        map.put("resultCode", existed ? -1 : 0);
        if (existed) {
            map.put("errMsg", "Delete failed.");
        }
        return map;
    }

    @Override
    public List<HostDetail> getHostDetailListById(Long projectId) {
        HostInfo hostInfo = hostInfoRepository.findByProjectIdAndMasterFlag(projectId, 1);
        return hostInfo == null ? null : HostUtil.hostInfo2HostDetailList(hostInfo);
    }
}
