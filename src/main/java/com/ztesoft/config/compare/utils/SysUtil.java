package com.ztesoft.config.compare.utils;

import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostInfo;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * 系统工具类，常用路径生成，windows路径转换
 *
 * @author wuyaxiong
 */
public class SysUtil {

    private static final String MASTER = "master";
    private static final String SLAVE = "slave";

    /**
     * 获取应用运行数据文件目录
     *
     * @return
     */
    public static String getRootPath() {
        String usrHome = getPwd() + File.separator + "data" + File.separator;
        return usrHome;
    }

    /**
     * 获取应用工作目录
     *
     * @return
     */
    public static String getPwd() {
        return System.getProperty("user.dir");
    }

    public static String getReportPath() {
        return getPwd() + File.separator + "report" + File.separator;
    }

    /**
     * 根据服务器信息
     *
     * @param projectName
     * @return
     */
    public static String getBasePath(String projectName) {
        return getRootPath() + projectName + File.separator;
    }

    public static String getTempPath(String projectName) {
        return getRootPath() + projectName + File.separator + "temp" + File.separator;
    }

    /**
     * 根据项目名，服务器ip地址生成路径
     * @param projectName
     * @param hostInfo
     * @return
     */
    public static String getPathByProjectAndHost(String projectName, HostInfo hostInfo) {
        String temp = hostInfo.getUser() + "@" + hostInfo.getHostIp() + "_" + hostInfo.getPort();
        return getBasePath(projectName) + temp + File.separator;
    }

    public static String changePath2Windows(String path) {
        return path.replaceAll("/", "\\" + File.separator).
                replaceAll("\\\\", "\\" + File.separator);
    }

    public static String camel2SingleWord(String str) {
        if(StringUtils.isEmpty(str)) {
            return "";
        }
        StringBuilder rs = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i == 0) {
                rs.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                rs.append(" ").append(c);
            } else {
                rs.append(c);
            }
        }
        return rs.toString();
    }
}
