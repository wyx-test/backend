package com.ztesoft.config.compare.controller;

import com.sun.corba.se.impl.orbutil.concurrent.SyncUtil;
import com.ztesoft.config.compare.utils.FileUtil;
import com.ztesoft.config.compare.utils.SysUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping(value = "/download")
public class FileDownloadController {
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadController.class);

//    @GetMapping(value = "file/{fileName}")
    public void downloadFile(@PathVariable String fileName, HttpServletResponse response) {
        File path = null;
        String reportPath = SysUtil.getReportPath();
        response.setContentType("application/octet-stream");
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
            path = new File(reportPath);
            String filePath = reportPath + fileName;
            os = response.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(new File(filePath)));
            int i = bis.read(buff);
            while (i != -1) {
                os.write(buff, 0, buff.length);
                os.flush();
                i = bis.read(buff);
            }
        } catch (FileNotFoundException e1) {
            //e1.getMessage()+"系统找不到指定的文件";
//            return "系统找不到指定的文件";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        return "success";
    }
    @GetMapping(value = "file/{fileName}")
    public ResponseEntity<byte[]> export(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        String reportPath = SysUtil.getReportPath();
        HttpHeaders headers = new HttpHeaders();
        File file = new File(reportPath + fileName);

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        InputStream in = new FileInputStream(file);
        byte[] bi =  new byte[in.available()];
        in.read(bi);
        ResponseEntity<byte[]> res = new ResponseEntity<>(bi,
                headers, HttpStatus.CREATED);
        in.close();
        return res;

    }
}
