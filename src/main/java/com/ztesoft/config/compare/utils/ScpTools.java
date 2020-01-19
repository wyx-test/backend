package com.ztesoft.config.compare.utils;

import ch.ethz.ssh2.*;
import com.ztesoft.config.compare.entity.FileCollect;
import com.ztesoft.config.compare.entity.HostInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SCP工具类，本地和服务器之间传输文件
 */
public class ScpTools {
    private static Logger logger = LoggerFactory.getLogger(ScpTools.class);
    private static String CHAR_SET = "UTF-8";
    private static final int TIME_OUT = 1000 * 2 * 60; // 2分钟超时
    private final static Log log = LogFactory.getLog(ScpTools.class);


    /**
     * 私有化默认构造函数
     * 实例化对象只能通过getInstance
     */
    private ScpTools() {

    }

    /**
     * 收集远程服务器文件夹到本地
     *
     * @param isWin
     * @param targetDir
     * @param excludeFiles
     * @param basePath
     * @param connection
     * @return
     */
    public static Map<String, Object> getRemoteDir(boolean isWin, String targetDir, List<String> excludeFiles, String basePath, Connection connection) {
        logger.info("begin to get dir to local:==================");
        SFTPv3Client sftpv3Client = null;
        logger.info("basePath: ： ================" + basePath);
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            sftpv3Client = new SFTPv3Client(connection);
            //查看目标服务器此文件夹是否存在
            Boolean b = isDir(sftpv3Client, targetDir);
            if (!b) {
                System.out.println("文件夹不存在");
                map.put("resultCode", -1);
                map.put("errMsg", "target directory is not exists;");
                return map;
            } else {
                logger.info("服务器目标文件夹：" + targetDir);
                //获取目标文件夹中所有文件
                List<SFTPv3DirectoryEntry> v = sftpv3Client.ls(targetDir);
                logger.info("获取到的文件个数为： " + v.size());

                for (SFTPv3DirectoryEntry entry : v) {
                    logger.info("file name: " + entry.filename);
                    SFTPv3FileAttributes attributes = entry.attributes;
                    String fileName = entry.filename;
                    String target = targetDir + "/" + fileName;
                    if (!attributes.isRegularFile() ||
                            (excludeFiles != null && excludeFiles.contains(fileName)) ||
                            fileName.endsWith(".bak")
                    ) {
                        continue;
                    }
                    String localFile = basePath + targetDir + File.separator + fileName;
                    logger.info("begin to scp dir to local,fileName: " + target + "; localFile: " + localFile);
                    list.add(ScpTools.getRemoteFile(isWin, target, localFile, connection));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        map.put("resultCode", 0);
        map.put("dirResult", list);
        return map;
    }

    /**
     * 判断指定路径是否为文件夹
     *
     * @param sftpv3Client
     * @param path
     * @return
     */
    public static Boolean isDir(SFTPv3Client sftpv3Client, String path) {
        if (sftpv3Client != null && path != null && path.length() != 0) {
            SFTPv3FileAttributes sFTPv3FileAttributes;
            try {
                sFTPv3FileAttributes = sftpv3Client.lstat(path);
            } catch (IOException e) {
                System.out.println("文件加不存在：" + e.getLocalizedMessage());
                return false;
            }
            return sFTPv3FileAttributes.isDirectory();
        }
        return false;
    }


    /**
     * 获取远程文件到本地
     *
     * @param remoteFile 目标服务器文件路径
     * @param newFile    保存到本地的路径
     * @param connection 服务器连接
     * @return
     */
    public static Map<String, Object> getRemoteFile(boolean isWin, String remoteFile, String newFile, Connection connection) {
        Map<String, Object> map = new HashMap<>();
        if (isWin) {
            newFile = SysUtil.changePath2Windows(newFile);
        }
        SCPClient scpClient;
        SCPInputStream sis = null;
        FileOutputStream fos = null;
        int index = newFile.lastIndexOf(File.separator);
        String newPath = newFile.substring(0, index);
        String fileName = newFile.substring(index + 1);
        map.put("fileName", fileName);
        map.put("localPath", newFile);
        try {
            scpClient = connection.createSCPClient();
            sis = scpClient.get(remoteFile);
            File f = new File(newPath);
            if (!f.exists()) {
                f.mkdirs();
            }
            File file = new File(newFile);
            fos = new FileOutputStream(newFile);
            byte[] b = new byte[4096];
            int i;
            while ((i = sis.read(b)) != -1) {
                fos.write(b, 0, i);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            map.put("resultCode", -1);
            map.put("errMsg", e.getMessage());
            return map;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (sis != null) {
                    sis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("download ok");
        map.put("resultCode", "0");
        return map;
    }

    /**
     * 备份目标服务器文件
     *
     * @param target
     * @param connection
     * @return
     */
    public static String[] backupFile(String target, Connection connection) {
        int lastIndex = target.lastIndexOf("/");
        String remotePath = target.substring(0, lastIndex);
        String bakName = target.substring(lastIndex + 1);
//        bak.`date +%y%m%d`.tar.gz
        String cmd = "cd " + remotePath + "; cp " + bakName + " " + bakName + "_`date +%Y%m%d%H%M%S`.bak " + ";";
        logger.debug("backup file cmd: " + cmd);
        String[] temp = executeCmd(cmd, connection);
        String resultCode = temp[2];
        if ("0".equals(resultCode)) {
            logger.debug("backup file result msg: " + temp[0]);
        } else {
            logger.info("backup file err msg: " + temp[1]);
        }
        return temp;
    }

    /**
     * 备份目标服务器文件夹
     *
     * @param target
     * @param connection
     * @return
     */
    public static String backupDir(String target, Connection connection) {
        logger.info("ScpTools.backupDir:" + target);
        String remotePath = target.substring(0, target.lastIndexOf("/"));
        String bakName = target.substring(target.lastIndexOf("/") + 1);
//        bak.`date +%y%m%d`.tar.gz
        String cmd = "cd " + remotePath + "; tar -zcf " + bakName + "_`date +%Y%m%d%H%M%S`.tar.gz " + bakName + ";";
        logger.info("backupDir cmd: " + cmd);
        String[] temp = executeCmd(cmd, connection);
        return temp[2];
    }

    /**
     * 获取服务器上相应文件的流
     *
     * @param remoteFile            文件名
     * @param remoteTargetDirectory 文件路径
     * @return
     * @throws IOException
     */
//    public SCPInputStream getStream(String remoteFile, String remoteTargetDirectory) throws IOException {
//        Connection connection = new Connection(ip, port);
//        connection.connect();
//        boolean isAuthenticated = connection.authenticateWithPassword(name, password);
//        if (!isAuthenticated) {
//            System.out.println("连接建立失败");
//            return null;
//        }
//        SCPClient scpClient = connection.createSCPClient();
//        return scpClient.get(remoteTargetDirectory + "/" + remoteFile);
//    }

    /**
     * 上传文件到指定服务器
     *
     * @param f               文件对象
     * @param remoteDirectory 上传路径
     * @param remoteFileName  远程文件名
     * @param connection      链接
     * @param mode            默认为null
     * @return
     */
    public static Map<String, Object> uploadFile(File f, String remoteDirectory, String remoteFileName, Connection connection, String mode) {
        Map<String, Object> map = new HashMap<>();
        System.out.println("begin to upload file");
        logger.info("fileName: " + f.getAbsolutePath() + "=====" + f.getName());
        logger.info("remote Path: " + remoteDirectory);
        SCPClient scpClient = null;
        SCPOutputStream os = null;
        FileInputStream fis = null;
        try {
            scpClient = new SCPClient(connection);
            os = scpClient.put(remoteFileName, f.length(), remoteDirectory, mode);
            byte[] b = new byte[4096];
            fis = new FileInputStream(f);
            int i;
            while ((i = fis.read(b)) != -1) {
                os.write(b, 0, i);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            map.put("resultCode", -1);
            map.put("errMsg", e.getMessage());
            return map;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("trans local file [" + f.getAbsolutePath() + "] to" + "remote file [" + remoteDirectory + "/" + remoteFileName + "] success");
        map.put("resultCode", 0);
//        f.getPath();
        return map;
    }

    /**
     * 在目标服务器执行shell命令
     *
     * @param cmd
     * @param conn
     * @return 返回结果数组
     */
    public static String[] executeCmd(String cmd, Connection conn) {
        System.out.println("begin to execute command");
        System.out.println("command: " + cmd);
        InputStream stdOut = null;
        InputStream stdErr = null;
        String outStr = "";
        String outErr = "";
        int outStatus = -1;
        String[] rt = new String[3];
        try {
            // Open a new {@link Session} on this connection
            Session session = conn.openSession();
            // Execute a command on the remote machine.
            session.execCommand(cmd);
            stdOut = new StreamGobbler(session.getStdout());
            outStr = processStream(stdOut, CHAR_SET);
            stdErr = new StreamGobbler(session.getStderr());
            outErr = processStream(stdErr, CHAR_SET);
            outStatus = session.getExitStatus();
            session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
            log.info("outStr=" + outStr.trim() + ", outErr=" + outErr.trim() + ", exitStatus="
                    + outStatus);
            rt[0] = outStr.trim();
            rt[1] = outErr.trim();
            rt[2] = String.valueOf(outStatus);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
//                if (conn != null) {
//                    conn.close();
//                }
                if (stdOut != null) {
                    stdOut.close();
                }
                if (stdErr != null) {
                    stdErr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rt;
    }

    /**
     * Description: <br>
     * 按指定字符集转换输入流
     *
     * @param in
     * @param charset
     * @return
     * @throws Exception <br>
     * @author XXX<br>
     * @taskId <br>
     */
    private static String processStream(InputStream in, String charset) throws Exception {
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while (in.read(buf) != -1) {
            sb.append(new String(buf, charset));
        }

        return sb.toString();
    }

    public static FileCollect getRemoteFile(FileCollect fileCollect, Connection connection) {
        String localPath = fileCollect.getLocalPath();
        SCPClient scpClient;
        SCPInputStream sis = null;
        FileOutputStream fos = null;
        int index = localPath.lastIndexOf(File.separator);
        String newPath = localPath.substring(0, index);
        logger.info("local directory path: " + newPath);
        try {
            scpClient = connection.createSCPClient();
            sis = scpClient.get(fileCollect.getRemotePath());
            File f = new File(newPath);
            if (!f.exists()) {
                f.mkdirs();
            }
            fos = new FileOutputStream(localPath);
            byte[] b = new byte[4096];
            int i;
            while ((i = sis.read(b)) != -1) {
                fos.write(b, 0, i);
            }
            fos.flush();
        } catch (IOException e) {
            fileCollect.setStatus(-1);
            logger.warn("scp file 2 local failed: " + e.getMessage());
            fileCollect.setStatus(-1);
            fileCollect.setErrMsg(e.getMessage());
            return fileCollect;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (sis != null) {
                    sis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("download ok");
        fileCollect.setStatus(0);
        return fileCollect;
    }

    /**
     * 传输本地文件到服务器
     *
     * @param localFile 本地文件
     * @param target    目标路径
     * @param host      目标服务器ip
     * @param user      用户
     * @param passwd    密码
     * @return
     */
    public static Map<String, Object> scpFile2Server(String localFile, String target, String host, String user, String passwd) {
        Connection connection = new Connection(host);
        Map<String, Object> map = new HashMap<>();
        try {
            connection.connect();
            System.out.println("开始登录");
            boolean isAuthenticated = connection.authenticateWithPassword(user, passwd);
            if (!isAuthenticated) {
                map.put("resultCode", -1);
                map.put("errMsg", "connect to server failed.Authentication failed.");
                return map;
            }
        } catch (IOException e) {
            e.printStackTrace();
            map.put("resultCode", -1);
            map.put("errMsg", "connect to server failed.");
            return map;
        }
        File file = new File(localFile);
        String[] retArr = ScpTools.backupFile(target, connection);

        String retCode = retArr[2];
                System.out.println("backup return code: " + retCode);
        if (!retCode.equals("0")) {
            map.put("fileName", file.getName());
            map.put("resultCode", -1);
            map.put("errMsg", "backup file failed [" + retArr[1] + "]; The file won't transfer to target server.");
            //        todo  不管成功与否 都将文件推送过去
            return map;
        }
//        todo
        int lastIndex = target.lastIndexOf("/");
        String remotePath = target.substring(0, lastIndex);
        String remoteFileName = target.substring(lastIndex + 1);
        Map<String, Object> result = ScpTools.uploadFile(file, remotePath, remoteFileName, connection, null);
        connection.close();
//        file.delete();
        return result;
    }
}
