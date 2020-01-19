package com.ztesoft.config.compare.service.impl;

import com.ztesoft.config.compare.dto.FileSyncInfo;
import com.ztesoft.config.compare.entity.FileCollect;
import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostInfo;
import com.ztesoft.config.compare.entity.Project;
import com.ztesoft.config.compare.repository.FileCollectRepository;
import com.ztesoft.config.compare.repository.FileInfoRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.repository.ProjectRepository;
import com.ztesoft.config.compare.service.FileCompareService;
import com.ztesoft.config.compare.service.FileSyncService;
import com.ztesoft.config.compare.utils.FileUtil;
import com.ztesoft.config.compare.utils.HostUtil;
import com.ztesoft.config.compare.utils.ScpTools;
import com.ztesoft.config.compare.utils.SysUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class FileSyncServiceImpl implements FileSyncService {


    @Value("${sysInfo}")
    private String sysInfo;

    private static Logger logger = LoggerFactory.getLogger(FileCompareServiceImpl.class);
    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    private HostInfoRepository hostInfoRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FileCollectRepository fileCollectRepository;

    @Autowired
    private FileCompareService fileCompareService;
    @Override
    public Map<String, Object> syncSingleFile(Long id) {
        FileCollect fileCollect = fileCollectRepository.getOne(id);
        Project project = projectRepository.getOne(fileCollect.getProjectId());
        HostInfo hostInfo = hostInfoRepository.getOne(fileCollect.getHostId());
        FileInfo fileInfo = fileInfoRepository.getOne(fileCollect.getFileId());
        Map<String, String> hostMap = HostUtil.hostInfo2Map(hostInfo);
        String bathPath = SysUtil.getBasePath(project.getName());
        FileSyncInfo fileSyncInfo = getSyncInfo(fileCollect);
        String tempPath = SysUtil.getPathByProjectAndHost(project.getName(), hostInfo) + "temp" + File.separator;
        String localFile = null;
        switch (fileSyncInfo.getType()) {
            case 1:
//                生成临时配置文件
                localFile = FileUtil.generatePropertiesFile(fileSyncInfo, tempPath);
                break;
            case 2:
//                生成临时配置文件
                localFile = FileUtil.generateIniFile(fileSyncInfo,tempPath);
                break;
            case 0:
                localFile = fileSyncInfo.getSource();
                break;
            default:
                break;
        }
        //                传输到指定位置
        logger.info("localFile: " + localFile);
        String password = hostMap.get("password");
        return ScpTools.scpFile2Server(localFile, fileSyncInfo.getRemotePath(), hostMap.get("hostIp"),
                hostMap.get("user"), password);
    }



    private static Map<String, Map<String, String>> mergeIniMap(Map<String, Map<String, String>> sourceMap, List<Map<String, String>> valueList) {
        if (valueList == null || valueList.size() == 0) {
            return sourceMap;
        }
        for (Map<String, String> map : valueList) {
            String sectionName = map.get("sectionName");
            if (sourceMap.containsKey(sectionName)) {
                logger.info("find a sectionName: " + sectionName);
                Map<String, String> subsMap = sourceMap.get(sectionName);
                String key = map.get("key");
                logger.info("key-----------------`````" + key);
                if (subsMap.containsKey(key)) {
                    logger.info("find a key [" + sectionName + "]." + key + "=" + map.get("value"));
                    System.out.println(subsMap.get(key));
                    subsMap.put(key, map.get("value"));
                    System.out.println(subsMap.get(key));
                }
            }
        }
        return sourceMap;
    }

    private FileSyncInfo getSyncInfo(FileCollect fileCollect) {
        Project project = projectRepository.getOne(fileCollect.getProjectId());
        HostInfo hostInfo = hostInfoRepository.getOne(fileCollect.getHostId());
        FileInfo fileInfo = fileInfoRepository.getOne(fileCollect.getFileId());
        HostInfo masterHost = hostInfoRepository.findByProjectIdAndMasterFlag(fileCollect.getProjectId(), 1);
        FileCollect masterCollect = fileCollectRepository.findByHostIdAndFileId(masterHost.getHostId(), fileCollect.getFileId());

        FileSyncInfo fileSyncInfo = new FileSyncInfo();
        fileSyncInfo.setId(fileInfo.getFileId());
        fileSyncInfo.setSource(masterCollect.getLocalPath());
        fileSyncInfo.setTarget(fileCollect.getLocalPath());
        fileSyncInfo.setFileName(FileUtil.getFileName(fileInfo.getGeneralPath()));
        fileSyncInfo.setHostInfo(hostInfo);
        fileSyncInfo.setType(fileInfo.getType());
        fileSyncInfo.setRemotePath(fileCollect.getRemotePath());
        fileSyncInfo.setSpecialValueStr(fileInfo.getSpecialValue());
        return fileSyncInfo;
    }
}
