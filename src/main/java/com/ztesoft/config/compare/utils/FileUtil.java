package com.ztesoft.config.compare.utils;

import com.alibaba.fastjson.JSON;
import com.ztesoft.config.compare.dto.FileCompareInfo;
import com.ztesoft.config.compare.dto.FileSyncInfo;
import com.ztesoft.config.compare.dto.ReplaceValue;
import com.ztesoft.config.compare.dto.SpecialValue;
import com.ztesoft.config.compare.entity.*;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

/**
 * 文件工具类，
 */
public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static String CHARSET = "UTF-8";


    /**
     * 获取文件内容编码
     *
     * @param fileName
     * @return
     */
    public static String getFileEncoding(String fileName) {
        byte[] buf = new byte[4096];
        FileInputStream fis = null;
        String encoding = "utf-8";
        try {
            fis = new FileInputStream(fileName);
            // (1)
            UniversalDetector detector = new UniversalDetector(null);

            // (2)
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            // (3)
            detector.dataEnd();
            // (4)
            encoding = detector.getDetectedCharset();
            // (5)
            detector.reset();
            if (encoding != null) {
                System.out.println("Detected encoding = " + encoding);
            } else {
                System.out.println("No encoding detected.");
            }

            return encoding == null ? "utf-8" : encoding;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encoding;
    }

    /**
     * 根据map键值对，生成properties配置文件
     *
     * @param fileSyncInfo
     * @return
     */
    public static String generatePropertiesFile(FileSyncInfo fileSyncInfo, String tempPath) {
        logger.info("=====begin to generate propertied file:" + fileSyncInfo.getFileName());
        Map<String, String> sourceMap = PropertiesFileUtil.getPropertyMap(fileSyncInfo.getSource());
        Map<String, String> targetMap = PropertiesFileUtil.getPropertyMap(fileSyncInfo.getTarget());
        List<SpecialValue> specialValues = JSON.parseArray(fileSyncInfo.getSpecialValueStr(), SpecialValue.class);
        Map<String, SpecialValue> valueMap = FileInfoUtil.transList2Map(specialValues, HostUtil.hostInfo2Map(fileSyncInfo.getHostInfo()));
        Map<String, String> resultMap = mergeMap(sourceMap, targetMap, valueMap);

        logger.info("=====begin method 2 map merge:-----------------");

        StringBuilder stringBuffer = new StringBuilder();
        String filePath = getTempFilePath(tempPath, fileSyncInfo.getFileName());
        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
            stringBuffer.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        String content = stringBuffer.toString();
//        logger.info("=====file content: " + content);
        String fileName = writeContent2File(content, filePath);
        logger.info("=====write properties file end===== fileName; " + fileName);
        return fileName;
    }

    private static void printMap(Map<String, String> valueMap) {
        logger.info("begin to print value map : =========================");
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            logger.info(entry.getKey() + "=======" + entry.getValue());
        }
    }


    private static Map<String, Map<String, String>> mergeIniMap4Exists(Map<String, Map<String, String>> sourceMap, Map<String, Map<String, String>> targetMap) {
        Map<String, Map<String, String>> result = new HashMap<>();
        Set<String> totalSet = new HashSet<>();
        totalSet.addAll(sourceMap.keySet());
        totalSet.addAll(targetMap.keySet());
        for (String key : totalSet) {
            if (sourceMap.containsKey(key) && !targetMap.containsKey(key)) {
                result.put(key, sourceMap.get(key));
            } else if (sourceMap.containsKey(key) && targetMap.containsKey(key)) {
                Map<String, String> sourceTemp = sourceMap.get(key);
                Map<String, String> targetTemp = targetMap.get(key);
                Set<String> tempSet = new HashSet<>();
                tempSet.addAll(sourceTemp.keySet());
                tempSet.addAll(targetTemp.keySet());
                for (String tempKey : tempSet) {
                    if (sourceTemp.containsKey(tempKey) && !targetTemp.containsKey(tempKey)) {
                        targetTemp.put(tempKey, sourceTemp.get(tempKey));
                    }
                }
                result.put(key, targetTemp);
            } else {
                result.put(key, targetMap.get(key));
            }
        }
        return result;
    }

    /**
     * 根据文件信息，创建临时文件父目录，返回临时文件路径
     *
     * @param fileName 文件信息
     * @return 临时文件路径
     */
    private static String getTempFilePath(String tempPath, String fileName) {
//        todo clear temp dir
//        deleteDir(tempPath);
        System.out.println("====================================TempPath: " + tempPath);
        File file = new File(tempPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return tempPath + fileName;
    }

    /**
     * 根据源文件ini，目标文件ini，配置文件的map，融合成结果map
     *
     * @param sourceMap
     * @param targetMap
     * @param valueMap
     * @return
     */
    private static Map<String, Map<String, String>> mergeIniMap(Map<String, Map<String, String>> sourceMap,
                                                                Map<String, Map<String, String>> targetMap,
                                                                Map<String, Map<String, SpecialValue>> valueMap) {
        if (valueMap == null || valueMap.size() == 0) {
            return sourceMap;
        }
        for (Map.Entry<String, Map<String, SpecialValue>> entry : valueMap.entrySet()) {
            String key = entry.getKey();
            for (Map.Entry<String, SpecialValue> innerEntry : entry.getValue().entrySet()) {
                String innerKey = innerEntry.getKey();
                SpecialValue specialValue = innerEntry.getValue();
                switch (specialValue.getMethod()) {
                    case 1:
                        if (targetMap.containsKey(key)) {
                            Map<String, String> temp = targetMap.get(key);
                            if (temp.containsKey(innerKey)) {
                                String value = temp.get(innerKey);
                                if (sourceMap.containsKey(key)) {
                                    Map<String, String> sourceTemp = sourceMap.get(key);
                                    sourceTemp.put(innerKey, value);
                                    sourceMap.put(key, sourceTemp);
                                } else {
                                    Map<String, String> sourceTemp = new HashMap<>();
                                    sourceTemp.put(innerKey, value);
                                    sourceMap.put(key, sourceTemp);
                                }
                            } else {
                                if (sourceMap.containsKey(key)) {
                                    Map<String, String> sourceTemp = sourceMap.get(key);
                                    sourceTemp.remove(innerKey);
                                    sourceMap.put(key, sourceTemp);
                                }
                            }
                        }
                        break;
                    case 2:
                        if (targetMap.containsKey(key)) {
                            Map<String, String> temp = targetMap.get(key);
                            if (temp.containsKey(innerKey)) {
                                String value = temp.get(innerKey);
                                if (sourceMap.containsKey(key)) {
                                    Map<String, String> sourceTemp = sourceMap.get(key);
                                    sourceTemp.put(innerKey, value);
                                } else {
                                    Map<String, String> sourceTemp = sourceMap.get(key);
                                    sourceTemp.put(innerKey, value);
                                    sourceMap.put(key, sourceTemp);
                                }
                            }
                        }
                        break;
                    case 3:
                        if (sourceMap.containsKey(key)) {
                            Map<String, String> temp = sourceMap.get(key);
                            temp.remove(innerKey);
                            sourceMap.put(key, temp);
                        }
                        break;
                    case 4:
                        if (sourceMap.containsKey(key)) {
                            Map<String, String> temp = sourceMap.get(key);
                            temp.put(innerKey, specialValue.getValue());
                            sourceMap.put(key, temp);
                        } else {
                            Map<String, String> temp = new HashMap<>();
                            temp.put(innerKey, specialValue.getValue());
                            sourceMap.put(key, temp);
                        }
                        break;
                }
            }
        }
        return sourceMap;
    }

    private static void print(Map<String, Map<String, String>> sourceMap) {
        for (Map.Entry<String, Map<String, String>> entry : sourceMap.entrySet()) {
            System.out.println("[" + entry.getKey() + "]");
            Map<String, String> map = entry.getValue();
            for (Map.Entry<String, String> temp : map.entrySet()) {
                System.out.println(temp.getKey() + "=" + temp.getValue());
            }
        }
    }


    /**
     * 融合两个map，把target在source中不存在的值存入target，对于存在的值不足欧修改
     *
     * @param sourceMap 源端map
     * @param targetMap 目标端map
     * @return 结果map
     */
    private static Map<String, String> mergeMap4Exist(Map<String, String> sourceMap, Map<String, String> targetMap) {
        for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!targetMap.containsKey(key) && value != null) {
                logger.info("插入目标端：" + key + "--------" + value);
                targetMap.put(key, value);
            }
        }
        return targetMap;
    }

    /**
     * 融合properties两个map，把valuemap中在source中存在的值存入target
     *
     * @param sourceMap 源端map
     * @param valueMap  目标端map
     * @return 结果map
     */
    private static Map<String, String> mergeMap(Map<String, String> sourceMap,
                                                Map<String, String> targetMap,
                                                Map<String, SpecialValue> valueMap) {
        if (valueMap == null || valueMap.size() == 0) {
            return sourceMap;
        }
        for (Map.Entry<String, SpecialValue> entry : valueMap.entrySet()) {
            String key = entry.getKey();
            SpecialValue specialValue = entry.getValue();
            Integer method = specialValue.getMethod();
            switch (method) {
                case 1:
                    if (targetMap.containsKey(key)) {
                        sourceMap.put(key, targetMap.get(key));
                    } else {
                        sourceMap.remove(key);
                    }
                    break;
                case 2:
                    if (targetMap.containsKey(key)) {
                        sourceMap.put(key, targetMap.get(key));
                    } else if (sourceMap.containsKey(key)) {
                        sourceMap.put(key, sourceMap.get(key));
                    } else if (!sourceMap.containsKey(key)) {
                        sourceMap.put(key, "");
                    }
                    break;
                case 3:
                    sourceMap.remove(key);
                    break;
                case 4:
                    sourceMap.put(key, specialValue.getValue());
                    break;
                default:
                    break;
            }
        }
        return sourceMap;
    }

    private static Map<String, String> getPropValueMapFromString(FileInfo fileInfo, Map<String, String> hostMap) {
        // todo
//        String str = fileInfo.getValueMap();
        String str = "";
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        List<ReplaceValue> replaceValues = JSON.parseArray(str, ReplaceValue.class);
        Map<String, String> valueMap = new HashMap<>();

        for (ReplaceValue replaceValue : replaceValues) {
            String attrName = replaceValue.getAttrName();
            if (hostMap.containsKey(attrName)) {
                valueMap.put(replaceValue.getKey(), hostMap.get(attrName));
            }
        }
        logger.info("valueMap size: " + valueMap.size());
        for (String string : valueMap.keySet()) {
            logger.info("键值对：" + string + "-----" + valueMap.get(string));
        }
        return valueMap;
    }

    private static List<Map<String, String>> getIniValueMapFromString(String str, Map<String, String> hostMap) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        List<SpecialValue> replaceValues = JSON.parseArray(str, SpecialValue.class);
        List<Map<String, String>> tempList = new ArrayList<>(replaceValues.size());

        for (SpecialValue replaceValue : replaceValues) {
            String attrName = replaceValue.getValue();
            Map<String, String> map = new HashMap<>();
            if (hostMap.containsKey(attrName)) {
                map.put("sectionName", replaceValue.getSectionName());
                map.put("key", replaceValue.getKey());
                map.put("value", hostMap.get(attrName));
                tempList.add(map);
            }
        }
//        for (Map<String, String> map : tempList) {
//            System.out.println(map.get("sectionName"));
//            System.out.println(map.get("key"));
//            System.out.println(map.get("value"));
//        }
        return tempList;
    }

    /**
     * 根据map键值对，生成properties配置文件
     * key=value
     *
     * @param map      键值对
     * @param FilePath 文件目录
     */
    private static String generateIniFileByMap(Map<String, Map<String, String>> map, String FilePath) {
        StringBuilder stringBuffer = new StringBuilder();
        for (Map.Entry<String, Map<String, String>> section : map.entrySet()) {
            stringBuffer.append("[").append(section.getKey()).append("]\n");
            for (Map.Entry<String, String> entry : section.getValue().entrySet()) {
                stringBuffer.append("    ").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
            stringBuffer.append("\n");
        }
        String content = stringBuffer.toString();
//        logger.info("=====file content: " + content);
        String fileName = writeContent2File(content, FilePath);
        logger.info("=====generate ini file end===== fileName; " + fileName);
        return fileName;
    }

    private static String getContentFormFile(List<FileInfo> fileInfos, String rootPath, String hostInfo) {
        StringBuilder stringBuffer = new StringBuilder();
        System.out.println(fileInfos.size());
        logger.info("file count: " + fileInfos.size());
        for (FileInfo fileInfo : fileInfos) {
            stringBuffer.append(hostInfo).append(" ").
                    append(rootPath).append(" ").
                    append(fileInfo.getGeneralPath()).append("\n");
        }
        String content = stringBuffer.toString();
        FileOutputStream outputStream = null;
        OutputStreamWriter writer = null;
        String fileName = rootPath + File.separator + "config";
        String charset = "UTF-8";
        // 写字符换转成字节流
        try {
            outputStream = new FileOutputStream(fileName);
            writer = new OutputStreamWriter(
                    outputStream, charset);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("=====generateConfigFile end===== fileName; " + fileName);
        return fileName;
    }

    /**
     * 将字符串写入文件
     *
     * @param content
     * @param fileName
     * @return
     */
    public static String writeContent2File(String content, String fileName) {
        FileOutputStream outputStream = null;
        OutputStreamWriter writer = null;
        // 写字符换转成字节流
        try {
            outputStream = new FileOutputStream(fileName);
            writer = new OutputStreamWriter(
                    outputStream, CHARSET);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("=====write content 2 file end===== fileName; " + fileName);
        return fileName;
    }

    /**
     * 递归删除指定目录下的所有文件和目录
     *
     * @param path
     * @return
     */
    public static boolean deleteDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            return false;
        }

        String[] content = file.list();//取得当前目录下所有文件和文件夹
        if (content == null || content.length == 0) {
            return true;
        }
        for (String name : content) {
            File temp = new File(path, name);
            if (temp.isDirectory()) {//判断是否是目录
                deleteDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
                temp.delete();//删除空目录
            } else {
                if (!temp.delete()) {//直接删除文件
                    System.err.println("Failed to delete " + name);
                }
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
//        FileInfo fileInfo = new FileInfo();
//        fileInfo.setTarget("C:\\Users\\wuyaxiong\\Documents\\targetimp.ini");
//        fileInfo.setSource("C:\\Users\\wuyaxiong\\Documents\\sourceimp.ini");
//        fileInfo.setMethod(2);
//        fileInfo.setValueMap("[KPI].Path=port");
//        Map<String,String> hostmap = new HashMap<>();
//        hostmap.put("port","8888");
//        String basepath = "C:\\Users\\wuyaxiong\\Downloads";
//        generateIniFile(fileInfo,basepath,hostmap);

//        String a = "[{fdsgsgd}]";
//        System.out.println(a.startsWith("[{") && a.endsWith("}]"));

    }

    /**
     * 根据map键值对，生成properties配置文件
     * key=value
     *
     * @param fileSyncInfo 文件信息
     * @param tempPath     文件目录
     */
    public static String generateIniFile(FileSyncInfo fileSyncInfo, String tempPath) {
        logger.info("basePath: " + tempPath);

        logger.info("=====begin to generate propertied file:" + fileSyncInfo.getSource());
        Map<String, Map<String, String>> sourceMap = IniFileUtil.readIni(fileSyncInfo.getSource());
        Map<String, Map<String, String>> targetMap = IniFileUtil.readIni(fileSyncInfo.getTarget());
        List<SpecialValue> specialValues = JSON.parseArray(fileSyncInfo.getSpecialValueStr(), SpecialValue.class);
        Map<String, Map<String, SpecialValue>> valueMap = FileInfoUtil.transList2Map4Ini(specialValues, HostUtil.hostInfo2Map(fileSyncInfo.getHostInfo()));
        Map<String, Map<String, String>> resultMap = new HashMap<>();

        logger.info("=====begin method 2 map merge:-----------------");
//        Map<String, Map<String, String>> tempMap = mergeIniMap4Exists(sourceMap, targetMap);
//        Map<String, Map<String, String>> tempMap = sourceMap;
        resultMap = mergeIniMap(sourceMap, targetMap, valueMap);

        String filePath = getTempFilePath(tempPath, fileSyncInfo.getFileName());
        generateIniFileByMap(resultMap, filePath);
        logger.info("=====generateIniFile end===== fileName; " + filePath);
        return filePath;
    }

    public static String getFileName(String targetName) {
        int index = targetName.lastIndexOf(File.separator);
//        int index = targetName.lastIndexOf("/");
        return targetName.substring(index + 1);
    }

    public static byte[] readFileToByteArray(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        return new byte[in.available()];
    }
}
