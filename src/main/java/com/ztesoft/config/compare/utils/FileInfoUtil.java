package com.ztesoft.config.compare.utils;

import com.alibaba.fastjson.JSON;
import com.ztesoft.config.compare.dto.DeltaFileInfo;
import com.ztesoft.config.compare.dto.SpecialValue;
import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostInfo;
import com.ztesoft.config.compare.entity.Project;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;

public class FileInfoUtil {
    private FileInfoUtil() {
    }

    @Deprecated
    public static Map<String, String> transPropValueMap(String valueMap) {
        Map<String, String> map = new HashMap<>();
        String[] valueArr = valueMap.split("|");
        for (String str : valueArr) {
            String[] temp = str.split("=");
            map.put(temp[0], temp[1]);
        }
        return map;
    }

    /**
     * 删除文件路径最后的文件分隔符
     *
     * @param fileInfo
     */
    public static void deleteEndSeparator(FileInfo fileInfo) {
        String target = fileInfo.getGeneralPath();
        if (target.endsWith(File.separator)) {
            fileInfo.setGeneralPath(target.substring(0, target.length() - 1));
        }
    }

    public static Map<String, SpecialValue> transList2Map(List<SpecialValue> specialValues, Map<String, String> hostMap) {
        if (specialValues == null) {
            return null;
        }
        Map<String, SpecialValue> map = new HashMap<>(specialValues.size());
        for (SpecialValue specialValue : specialValues) {
            if (specialValue.getMethod() == 4 && hostMap.containsKey(specialValue.getValue())) {
                specialValue.setValue(hostMap.get(specialValue.getValue()));
            }
            map.put(specialValue.getKey(), specialValue);
        }
        return map;
    }

    public static Map<String, Map<String, SpecialValue>> transList2Map4Ini(List<SpecialValue> specialValues, Map<String, String> hostMap) {
        if (specialValues == null) {
            return new HashMap<>();
        }
        Map<String, Map<String, SpecialValue>> map = new HashMap<>(specialValues.size());
        for (SpecialValue specialValue : specialValues) {
            String section = specialValue.getSectionName();
            String key = specialValue.getKey();
            String value = specialValue.getValue();
            Integer method = specialValue.getMethod();
            if (method == 4 && hostMap.containsKey(specialValue.getValue())) {
                specialValue.setValue(hostMap.get(specialValue.getValue()));
            }
            if (map.containsKey(section)) {
                map.get(section).put(key, specialValue);
                continue;
            }
            Map<String, SpecialValue> temp = new HashMap<>(1);
            temp.put(key, specialValue);
            map.put(section, temp);
        }
        return map;
    }

    public static void main(String[] args) {
        SpecialValue s1 = new SpecialValue("test111", "a", 4, "hostIp");
        SpecialValue s2 = new SpecialValue("test111", "b", 1, "");
        SpecialValue s3 = new SpecialValue("test1", "cc", 3, "");
        List<SpecialValue> specialValues = new ArrayList<>();
        specialValues.add(s1);
        specialValues.add(s2);
        specialValues.add(s3);
        Map<String, String> hostMap = new HashMap<>();
        hostMap.put("hostIp", "127.0.0.1");
        Map<String, Map<String, SpecialValue>> map = transList2Map4Ini(specialValues, hostMap);
        System.out.println(map);
    }

    public static String getFilePath(HostInfo hostInfo, FileInfo fileInfo) {
        String generalPath = fileInfo.getGeneralPath();
        String deltaFileInfoStr = fileInfo.getDeltaFileInfos();
        if (StringUtils.isEmpty(deltaFileInfoStr)) {
            return generalPath;
        }
        List<DeltaFileInfo> deltaFileInfos = JSON.parseArray(deltaFileInfoStr, DeltaFileInfo.class);
        if (deltaFileInfos == null || deltaFileInfos.size() == 0) {
            return generalPath;
        }
        for (DeltaFileInfo deltaFileInfo : deltaFileInfos) {
            if (deltaFileInfo.getHostId().equals(hostInfo.getHostId())) {
                return deltaFileInfo.getPath();
            }
        }
        return generalPath;
    }
    public static String getAbsoluteFilePath(HostInfo hostInfo, FileInfo fileInfo) {
        if(hostInfo == null || hostInfo.getHostId() == null) {
            return "Source host is not configured.";
        }
        String generalPath = fileInfo.getGeneralPath();
        String homeDir = hostInfo.getHomeDir();
        if(!homeDir.endsWith("/")) {
            homeDir = homeDir + "/";
        }
        String deltaFileInfoStr = fileInfo.getDeltaFileInfos();
        if (StringUtils.isEmpty(deltaFileInfoStr)) {
            return homeDir + generalPath;
        }
        List<DeltaFileInfo> deltaFileInfos = JSON.parseArray(deltaFileInfoStr, DeltaFileInfo.class);
        if (deltaFileInfos == null || deltaFileInfos.size() == 0) {
            return homeDir + generalPath;
        }
        for (DeltaFileInfo deltaFileInfo : deltaFileInfos) {
            if (deltaFileInfo.getHostId().equals(hostInfo.getHostId())) {
                return homeDir + deltaFileInfo.getPath();
            }
        }
        return homeDir + generalPath;
    }
}
