package com.ztesoft.config.compare.utils;

import com.ztesoft.config.compare.dto.CompareReport;
import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

public class CSVUtil {
    private static final Logger logger = LoggerFactory.getLogger(CSVUtil.class);

    public static void write2CSV(List dataList, List header, String outputPath) {
        Appendable out = null;
        CSVPrinter printer = null;
        try {
            out = new PrintWriter("file.csv");
            printer = CSVFormat.DEFAULT.withHeader("userId", "userName")
                    .print(out);
            for (int i = 0; i < 10; i++) {
                printer.printRecord("userId" + i, "userName" + i);
            }
            printer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (printer != null) {
                    printer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        write2CSV(null, null, null);
    }

    public static String generateReportFile(List<CompareReport> compareReports) {
        Calendar calendar = Calendar.getInstance();
        String reportPath = SysUtil.getReportPath();
        File report = new File(reportPath);
        if (!report.exists()) {
            report.mkdirs();
        }
        String fileName = "report-" + calendar.getTimeInMillis() + ".csv";
        String filePath = reportPath + fileName;
        Appendable out = null;
        CSVPrinter printer = null;
        try {
            out = new PrintWriter(filePath);
            printer = CSVFormat.DEFAULT.withHeader(getHeader())
                    .print(out);
            for (CompareReport compareReport : compareReports) {
                HostInfo hostInfo = compareReport.getHostInfo();
                HostInfo masterHostInfo = compareReport.getMasterHostInfo();
                FileInfo fileInfo = compareReport.getFileInfo();
                String hostStr = hostInfo.getUser() + "@" + hostInfo.getHostIp() + ":" + hostInfo.getPort();
                String masterHostStr = "";
                if(masterHostInfo == null || masterHostInfo.getHostId()== null) {
                    masterHostStr = "Source host is not configured!";
                } else {
                     masterHostStr = masterHostInfo.getUser() + "@" + masterHostInfo.getHostIp() + ":" + masterHostInfo.getPort();
                }
                printer.printRecord(
                        compareReport.getProject().getName(),
                        fileInfo.getFileName(),
                        masterHostStr,
                        FileInfoUtil.getAbsoluteFilePath(masterHostInfo,fileInfo),
                        hostStr,
                        FileInfoUtil.getAbsoluteFilePath(hostInfo, fileInfo),
                        compareReport.getCompareResult(),
                        compareReport.getTotalCount(),
                        compareReport.getPassed(),
                        compareReport.getNotPassed(),
                        compareReport.getResultCode(),
                        compareReport.getMessage()
                );
            }
            printer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("generated csv file failed: " + e.getMessage());
            return null;
        } finally {
            try {
                if (printer != null) {
                    printer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    private static String[] getHeader() {
        String[] strings = {"Project Name", "File Name", "Source Host", "Source File Path",
                "Target Host", "Target File Path", "Result", "Total Count", "Passed Count",
                "Not Passed Count", "Result Code", "Message"};
        return strings;
    }
}
