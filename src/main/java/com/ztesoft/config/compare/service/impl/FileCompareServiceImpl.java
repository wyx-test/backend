package com.ztesoft.config.compare.service.impl;

import ch.ethz.ssh2.Connection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.difflib.text.DiffRow;
import com.ztesoft.config.compare.dto.CompareReport;
import com.ztesoft.config.compare.dto.ContentValueInfo;
import com.ztesoft.config.compare.dto.FileCompareInfo;
import com.ztesoft.config.compare.dto.SpecialValue;
import com.ztesoft.config.compare.entity.*;
import com.ztesoft.config.compare.repository.FileCollectRepository;
import com.ztesoft.config.compare.repository.FileInfoRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.repository.ProjectRepository;
import com.ztesoft.config.compare.service.FileCompareService;
import com.ztesoft.config.compare.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class FileCompareServiceImpl implements FileCompareService {
    private static final String UNIX_FILE_SEPARATOR = "/";
    private static final String WIN_FILE_SEPARATOR = "\\";


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


    @Override
    public Map<String, Object> syncFile2Server(Long projectId) {
        return null;
    }

    @Override
    public Map<String, Object> syncFile2ServerByHostId(Long hostId) {
        return null;
    }

    @Override
    public Map<String, Object> compareFile(Project project, HostInfo hostInfo, List<FileInfo> fileInfos) {
        return null;
    }

    @Override
    public Map<String, Object> collectFileByProjectId(Long projectId) {
        logger.info("begin to collect file by id: " + projectId);
        Map<String, Object> result = new HashMap<>();
        Project project = projectRepository.getOne(projectId);
        List<HostInfo> hostInfoList = hostInfoRepository.findByProjectId(projectId);
        if (hostInfoList == null || hostInfoList.size() == 0) {
            result.put("resultCode", -1);
            result.put("errMsg", "The project has no host, please add host first.");
            return result;
        }
        List<FileInfo> fileInfos = fileInfoRepository.findByProjectId(projectId);
        if (fileInfos.size() == 0) {
            result.put("resultCode", -1);
            result.put("errMsg", "The file has not configured, please add file info first.");
            return result;
        }
        for (HostInfo hostInfo : hostInfoList) {
            result.put(hostInfo.getHostIp(), collectFile(project, hostInfo, fileInfos));
        }
        return result;
    }

    @Override
    public Map<String, Object> compareFileByProject(Long projectId) {
        return null;
    }

    @Override
    public Map<String, Object> compareFileByFileCollect(Long id) {
        FileCollect fileCollect = fileCollectRepository.getOne(id);
        Map<String, Object> map = new HashMap<>();
        if (fileCollect.getStatus() == -1) {
            map.put("resultCode", -1);
            map.put("errMsg", "Target file [" + fileCollect.getRemotePath() + "] is not existed");
            return map;
        }
        FileCompareInfo fileCompareInfo = getFileCompare(fileCollect);
        if (fileCompareInfo == null) {
            map.put("resultCode", -1);
            map.put("errMsg", "Compare file failed, The project has not configured master host;");
            return map;
        }
        map = compareFile(fileCompareInfo);
        return map;
    }

    @Override
    public List<CompareReport> getReportFromCompareResult(Map<String, Map<String, Object>> compareResult) {
        List<CompareReport> reportResult = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : compareResult.entrySet()) {
            Long fileId = Long.valueOf(entry.getKey().substring(4));
            Optional<FileCollect> fileCollectOptional = fileCollectRepository.findById(fileId);
            if (!fileCollectOptional.isPresent()) {
                continue;
            }
            FileCollect fileCollect = fileCollectOptional.get();
            Project project = projectRepository.getOne(fileCollect.getProjectId());
            HostInfo hostInfo = hostInfoRepository.getOne(fileCollect.getHostId());
            HostInfo masterHostInfo = hostInfoRepository.findByProjectIdAndMasterFlag(fileCollect.getProjectId(),1);
            FileInfo fileInfo = fileInfoRepository.getOne(fileCollect.getFileId());
            CompareReport compareReport = new CompareReport();
            compareReport.setFileInfo(fileInfo);
            compareReport.setProject(project);
            compareReport.setHostInfo(hostInfo);
            compareReport.setMasterHostInfo(masterHostInfo);
            Map<String, Object> temp = entry.getValue();
            Integer resultCode = (Integer) temp.get("resultCode");
            compareReport.setResultCode(resultCode);
            if (resultCode == -1) {
                compareReport.setMessage((String) temp.get("errMsg"));
                compareReport.setCompareResult(false);
                reportResult.add(compareReport);
                continue;
            }
            Integer type = fileCollect.getType();
            switch (type) {
                case 0:
                    List<DiffRow> result = (List<DiffRow>) temp.get("result");
                    compareReport.setTotalCount(result.size());
                    Integer passedCount = getPassedCount(result);
                    compareReport.setPassed(passedCount);
                    compareReport.setCompareResult(passedCount == result.size());
                    compareReport.setNotPassed(getNotPassedCount(result));
                    break;
                case 1:
                case 2:
                    List<ContentValueInfo> contentValueInfos = (List<ContentValueInfo>) temp.get("result");
                    compareReport.setTotalCount(contentValueInfos.size());
                    Integer passCount = getPassedCount4ContentValue(contentValueInfos);
                    compareReport.setPassed(passCount);
                    compareReport.setCompareResult(passCount == contentValueInfos.size());
                    compareReport.setNotPassed(contentValueInfos.size() - passCount);
            }
            reportResult.add(compareReport);
        }
        return reportResult;
    }

    private Integer getPassedCount4ContentValue(List<ContentValueInfo> contentValueInfos) {
        int i = 0;
        for (ContentValueInfo contentValueInfo : contentValueInfos) {
            if (contentValueInfo.getStatus() == 0) {
                i++;
            }
        }
        return i;
    }

    private Integer getPassedCount(List<DiffRow> result) {
        int i = 0;
        for (DiffRow diffRow : result) {
            if ("EQUAL".equals(diffRow.getTag().toString())) {
                i++;
            }
        }
        return i;
    }

    private Integer getNotPassedCount(List<DiffRow> result) {
        int i = 0;
        for (DiffRow diffRow : result) {
            if ("EQUAL".equals(diffRow.getTag().toString())) {
                continue;
            }
            i++;
        }
        return i;
    }

    private Map<String, Object> compareFile(FileCompareInfo fileCompareInfo) {
        logger.info("=====begin to compare file by file:" + fileCompareInfo.getTarget());
        String sourceFile = fileCompareInfo.getSource();
        String targetFile = fileCompareInfo.getTarget();

        logger.info("target absolute path: " + targetFile);
        logger.info("fileName: " + sourceFile);
        logger.info("file type: " + fileCompareInfo.getType());
        File source = new File(sourceFile);
        Map<String, Object> map = new HashMap<>();
        if (!source.exists()) {
            map.put("resultCode", -1);
            map.put("errMsg", "the source File [" + fileCompareInfo.getSource() + "] is not exists");
            return map;
        }
        File target = new File(targetFile);
        if (!target.exists()) {
            map.put("resultCode", -1);
            map.put("errMsg", "the target File [" + fileCompareInfo.getTarget() + "] is not exists");
            return map;
        }
        switch (fileCompareInfo.getType()) {
            case 1:
                return compareProperties(fileCompareInfo);
            case 2:
                return compareIni(fileCompareInfo);
            case 0:
                return compareOther(fileCompareInfo);
            default:
                return null;
        }

    }

    private Map<String, Object> compareOther(FileCompareInfo fileCompareInfo) {
        logger.info("compare other file: ========================== begin" + fileCompareInfo.getTarget());
        Map<String, Object> map = TextFileCompareUtil.compareTextFile(fileCompareInfo);
        logger.info("compare other file: ========================== end" + fileCompareInfo.getTarget());
        return map;

    }

    private Map<String, Object> compareIni(FileCompareInfo fileCompareInfo) {
        logger.info("begin to compare ini file: " + fileCompareInfo.getSource());
        String sourceFile = fileCompareInfo.getSource();
        String targetFile = fileCompareInfo.getTarget();
        Map<String, Map<String, String>> sourceMap = IniFileUtil.readIni(sourceFile);
        Map<String, Map<String, String>> targetMap = IniFileUtil.readIni(targetFile);
        List<SpecialValue> specialValues = JSON.parseArray(fileCompareInfo.getSpecialValueStr(), SpecialValue.class);
        Map<String, Map<String, SpecialValue>> valueMap = FileInfoUtil.transList2Map4Ini(specialValues, fileCompareInfo.getHostMap());
        return this.compareIniMap(sourceMap, targetMap, valueMap);
    }

    /**
     * 比较ini文件的hashMap
     *
     * @param sourceMap 源端sourceMap
     * @param targetMap 目标端targetMap
     * @return
     */
    private Map<String, Object> compareIniMap(Map<String, Map<String, String>> sourceMap,
                                              Map<String, Map<String, String>> targetMap,
                                              Map<String, Map<String, SpecialValue>> valueMap) {
        List<ContentValueInfo> list = new ArrayList<>();
        Set<String> totalSet = new HashSet<>();
        totalSet.addAll(sourceMap.keySet());
        totalSet.addAll(targetMap.keySet());
        Map<String, Object> map = new HashMap<>();
        for (String key : totalSet) {
            Map<String, String> sourceValueMap = sourceMap.get(key);
            Map<String, String> targetValueMap = targetMap.get(key);
            list.addAll(this.dealIniSection(key, sourceValueMap, targetValueMap, valueMap.get(key)));
        }
        map.put("result", list);
        map.put("resultCode", 0);
        map.put("type", 2);
        return map;
    }

    private List<ContentValueInfo> dealIniSection(String sectionName, Map<String, String> sourceValueMap,
                                                  Map<String, String> targetValueMap,
                                                  Map<String, SpecialValue> valueMap) {
        List<ContentValueInfo> contentValueInfos = new ArrayList<>();
        Set<String> totalSet = new HashSet<>();
        boolean flag = true;
        if (sourceValueMap != null && targetValueMap != null) {
            totalSet.addAll(sourceValueMap.keySet());
            totalSet.addAll(targetValueMap.keySet());
            for (String key : totalSet) {
                String sourceValue = sourceValueMap.get(key);
                String targetValue = targetValueMap.get(key);
                ContentValueInfo contentValueInfo = new ContentValueInfo();
                contentValueInfo.setName(key);
                contentValueInfo.setSectionName(sectionName);
                contentValueInfo.setSourceValue(sourceValue);
                contentValueInfo.setTargetValue(targetValue);
                if (valueMap != null && valueMap.containsKey(key)) {
                    SpecialValue specialValue = valueMap.get(key);
                    Integer method = specialValue.getMethod();
                    switch (method) {
                        case 1:
                            contentValueInfo.setStatus(0);
                            contentValueInfo.setMethod("ignore");
                            break;
                        case 2:
                            contentValueInfo.setStatus(targetValue != null ? 0 : -1);
                            contentValueInfo.setMethod("Existed");
                            break;
                        case 3:
                            contentValueInfo.setStatus(targetValue == null ? 0 : -1);
                            contentValueInfo.setMethod("NotExisted");
                            break;
                        case 4:
                            if (targetValue == null) {
                                contentValueInfo.setStatus(-1);
                            } else {
                                contentValueInfo.setStatus(targetValue.equals(specialValue.getValue()) ? 0 : -1);
                            }
                            contentValueInfo.setMethod("SpecialValue");
                            break;
                        default:
                            break;
                    }
                    contentValueInfos.add(contentValueInfo);
                    continue;
                }
                boolean status = getStatus(sourceValue, targetValue);
                contentValueInfo.setStatus(status ? 0 : -1);
                contentValueInfos.add(contentValueInfo);
            }
        } else if (sourceValueMap == null && targetValueMap != null) {
            totalSet.addAll(targetValueMap.keySet());
            for (String key : totalSet) {
                String targetValue = targetValueMap.get(key);
                ContentValueInfo contentValueInfo = new ContentValueInfo();
                contentValueInfo.setName(key);
                contentValueInfo.setSectionName(sectionName);
                contentValueInfo.setSourceValue(null);
                contentValueInfo.setTargetValue(targetValue);
                if (valueMap != null && valueMap.containsKey(key)) {
                    SpecialValue specialValue = valueMap.get(key);
                    Integer method = specialValue.getMethod();
                    switch (method) {
                        case 1:
                            contentValueInfo.setStatus(0);
                            contentValueInfo.setMethod("ignore");
                            break;
                        case 2:
                            contentValueInfo.setStatus(targetValue != null ? 0 : -1);
                            contentValueInfo.setMethod("Existed");
                            break;
                        case 3:
                            contentValueInfo.setStatus(targetValue == null ? 0 : -1);
                            contentValueInfo.setMethod("NotExisted");
                            break;
                        case 4:
                            if (targetValue == null) {
                                contentValueInfo.setStatus(-1);
                            } else {
                                contentValueInfo.setStatus(targetValue.equals(specialValue.getValue()) ? 0 : -1);
                            }
                            contentValueInfo.setMethod("SpecialValue");
                            break;
                        default:
                            break;
                    }
                    contentValueInfos.add(contentValueInfo);
                    continue;
                }
                boolean status = getStatus(null, targetValue);
                contentValueInfo.setStatus(status ? 0 : -1);
                contentValueInfos.add(contentValueInfo);
                if (flag || status) {
                    flag = false;
                }
                contentValueInfo.setStatus(-1);
                contentValueInfos.add(contentValueInfo);
            }
        } else if (sourceValueMap != null) {
            totalSet.addAll(sourceValueMap.keySet());
            for (String key : totalSet) {
                String sourceValue = sourceValueMap.get(key);
                ContentValueInfo contentValueInfo = new ContentValueInfo();
                contentValueInfo.setName(key);
                contentValueInfo.setSectionName(sectionName);
                contentValueInfo.setSourceValue(sourceValue);
                contentValueInfo.setTargetValue(null);
                String targetValue = null;
                if (valueMap != null && valueMap.containsKey(key)) {
                    SpecialValue specialValue = valueMap.get(key);
                    Integer method = specialValue.getMethod();
                    switch (method) {
                        case 1:
                            contentValueInfo.setStatus(0);
                            contentValueInfo.setMethod("ignore");
                            break;
                        case 2:
                            contentValueInfo.setStatus(-1);
                            contentValueInfo.setMethod("Existed");
                            break;
                        case 3:
                            contentValueInfo.setStatus(0);
                            contentValueInfo.setMethod("NotExisted");
                            break;
                        case 4:
                            contentValueInfo.setStatus(-1);
                            contentValueInfo.setMethod("SpecialValue");
                            break;
                        default:
                            break;
                    }
                    contentValueInfos.add(contentValueInfo);
                    continue;
                }
                boolean status = getStatus(sourceValue, null);
                if (flag || status) {
                    flag = false;
                }
                contentValueInfo.setStatus(-1);
                contentValueInfos.add(contentValueInfo);
            }
        }
        return contentValueInfos;
    }

    public Map<String, Object> compareProperties(FileCompareInfo fileCompareInfo) {
        Map<String, String> hostMap = fileCompareInfo.getHostMap();
        String sourceFile = fileCompareInfo.getSource();
        String targetFile = fileCompareInfo.getTarget();
        List<SpecialValue> specialValues = JSONObject.parseArray(fileCompareInfo.getSpecialValueStr(), SpecialValue.class);
//        todo ini文件需要另外处理
        Map<String, SpecialValue> valueMap = FileInfoUtil.transList2Map(specialValues, hostMap);
        File file = new File(targetFile);
        Map<String, Object> map = new HashMap<>();
        if (!file.exists()) {
            map.put("resultCode", -1);
            map.put("errMsg", "the target File [" + targetFile + "] is not exists");
            return map;
        }
        Map<String, String> sourceValueMap = PropertiesFileUtil.getPropertyMap(sourceFile);
        Map<String, String> targetValueMap = PropertiesFileUtil.getPropertyMap(targetFile);
        boolean flag = true;
        List<ContentValueInfo> contentValueInfos = new ArrayList<>();
        Set<String> totalSet = new HashSet<>();
        totalSet.addAll(sourceValueMap.keySet());
        totalSet.addAll(targetValueMap.keySet());
        for (String key : totalSet) {
            String sourceValue = sourceValueMap.get(key);
            String targetValue = targetValueMap.get(key);
            ContentValueInfo contentValueInfo = new ContentValueInfo();
            contentValueInfo.setName(key);
            contentValueInfo.setSourceValue(sourceValue);
            contentValueInfo.setTargetValue(targetValue);
            if (valueMap != null && valueMap.containsKey(key)) {
                SpecialValue specialValue = valueMap.get(key);
                Integer method = specialValue.getMethod();
                switch (method) {
                    case 1:
                        contentValueInfo.setStatus(0);
                        contentValueInfo.setMethod("ignore");
                        break;
                    case 2:
                        contentValueInfo.setStatus(targetValue != null ? 0 : -1);
                        contentValueInfo.setMethod("Existed");
                        break;
                    case 3:
                        contentValueInfo.setStatus(targetValue == null ? 0 : -1);
                        contentValueInfo.setMethod("NotExisted");
                        break;
                    case 4:
                        if (targetValue == null) {
                            contentValueInfo.setStatus(-1);
                        } else {
                            contentValueInfo.setStatus(targetValue.equals(specialValue.getValue()) ? 0 : -1);
                        }
                        contentValueInfo.setMethod("SpecialValue");
                        break;
                    default:
                        break;
                }
            } else {
                boolean isSame = getStatus(sourceValue, targetValue);
                if (!isSame && flag) {
                    flag = false;
                }
                contentValueInfo.setStatus(isSame ? 0 : -1);
            }
            contentValueInfos.add(contentValueInfo);
        }
        map.put("result", contentValueInfos);
        map.put("type", fileCompareInfo.getType());
        map.put("resultCode", 0);
        return map;
    }

    private boolean getStatus(String sourceValue, String targetValue) {
        if (targetValue == null && sourceValue == null) {
            return true;
        }
        if (targetValue == null || sourceValue == null) {
            return false;
        }
        return targetValue.equals(sourceValue);
    }

    private FileCompareInfo getFileCompare(FileCollect fileCollect) {
        FileCompareInfo fileCompareInfo = new FileCompareInfo();
        Project project = projectRepository.getOne(fileCollect.getProjectId());
        HostInfo hostInfo = hostInfoRepository.getOne(fileCollect.getHostId());
        HostInfo masterHost = hostInfoRepository.findByProjectIdAndMasterFlag(fileCollect.getProjectId(), 1);
        if (masterHost == null) {
            return null;
        }
        FileCollect masterCollect = fileCollectRepository.findByHostIdAndFileId(masterHost.getHostId(), fileCollect.getFileId());
        if (masterCollect == null) {
            return null;
        }
        FileInfo fileInfo = fileInfoRepository.getOne(fileCollect.getFileId());
        fileCompareInfo.setFileName(fileCollect.getFileName());
        fileCompareInfo.setId(fileCollect.getId());
        fileCompareInfo.setTarget(fileCollect.getLocalPath());
        fileCompareInfo.setSource(masterCollect.getLocalPath());
        fileCompareInfo.setType(fileCollect.getType());
        fileCompareInfo.setSpecialValueStr(fileInfo.getSpecialValue());
        fileCompareInfo.setHostMap(HostUtil.hostInfo2Map(hostInfo));
        return fileCompareInfo;
    }


    List<FileCollect> collectFile(Project project, HostInfo hostInfo, List<FileInfo> fileInfos) {
        boolean isWin = "win".equals(sysInfo);
        String projectName = project.getName();
        String localPath = SysUtil.getPathByProjectAndHost(projectName, hostInfo);
        List<Map<String, Object>> list = new ArrayList<>();
        List<FileCollect> fileCollects = new ArrayList<>();
        //        收集文件前先清空目录 todo
        FileUtil.deleteDir(localPath);
//        获得ssh2连接
        Connection connection = new Connection(hostInfo.getHostIp());
        fileCollectRepository.deleteByHostId(hostInfo.getHostId());
        try {
            connection.connect();
            logger.info("开始登录");
            String password = HostUtil.decryptDES(hostInfo.getPassword());
            boolean isAuthenticated = connection.authenticateWithPassword(hostInfo.getUser(), password);
            if (!isAuthenticated) {
                logger.warn("登录服务器失败！");
                return null;
            }
            logger.info("file size :" + fileInfos.size());
            for (FileInfo fileInfo : fileInfos) {
                FileCollect fileCollect = new FileCollect();
                fileCollect.setFileId(fileInfo.getFileId());
                fileCollect.setHostId(hostInfo.getHostId());
                fileCollect.setProjectId(project.getProjectId());
                fileCollect.setType(fileInfo.getType());
                String targetName = FileInfoUtil.getFilePath(hostInfo, fileInfo);
                fileCollect.setFileName(fileInfo.getFileName());
//                todo
                logger.info("localPath: " + localPath + ",targetName: " + targetName);
                String localFile = localPath + (targetName.startsWith("/") ? targetName.substring(1) : targetName);
                logger.info("begin to scp file to local, fileName: " + targetName + "; localFile: " + localFile);
                fileCollect.setLocalPath(isWin ? SysUtil.changePath2Windows(localFile) : localFile);
                fileCollect.setRemotePath(targetName);
                ScpTools.getRemoteFile(fileCollect, connection);
                fileCollects.add(fileCollect);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("登录服务器失败！");
            return null;
        } finally {
            connection.close();
        }
        fileCollectRepository.saveAll(fileCollects);
        return fileCollects;
    }
}
