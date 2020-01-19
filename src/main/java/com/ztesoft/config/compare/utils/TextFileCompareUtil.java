package com.ztesoft.config.compare.utils;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.ztesoft.config.compare.dto.FileCompareInfo;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本文件对比工具类
 */
public class TextFileCompareUtil {
    private static Logger logger = LoggerFactory.getLogger(TextFileCompareUtil.class);

    /**
     *
     * @param filename
     * @param cs
     * @return
     */
    private static List<String> fileToLines(String filename, Charset cs) {
        try {
            return Files.readAllLines(new File(filename).toPath(), cs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * multi liner
     */
    public static Map<String, Object> compareTextFile2table(FileCompareInfo fileCompareInfo) {
        String sourceFile = fileCompareInfo.getSource();
        String targetFile = fileCompareInfo.getTarget();
//        todo
        fileCompareInfo.setCharsetName("utf-8");
        Map<String, Object> result = new HashMap<>();
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "<del>")
                .newTag(f -> "<strong>")
                .build();

        List<DiffRow> rows;
        try {
            String originalEncoding = getFileEncoding(sourceFile);
            if(originalEncoding == null) {
                result.put("resultCode", -1);
                result.put("errMsg", "[" + sourceFile+ "] encoding is not found, cannot compare file.");
                return result;
            }
            String patchedEncoding = getFileEncoding(targetFile);
            if(patchedEncoding == null) {
                result.put("resultCode", -1);
                result.put("errMsg", "[" + targetFile+ "] encoding is not found, cannot compare file.");
                return result;
            }
            List<String> original = Files.readAllLines(new File(sourceFile).toPath(), Charset.forName(originalEncoding));
            List<String> patched = Files.readAllLines(new File(targetFile).toPath(), Charset.forName(patchedEncoding));
            rows = generator.generateDiffRows(original, patched);
        } catch (IOException | DiffException e) {
            e.printStackTrace();
            result.put("resultCode", -1);
            result.put("errMsg", e.getMessage());
            return result;
        }

        printTextCompareResult(rows);
        result.put("resultCode", 0);
        result.put("fileId", fileCompareInfo.getId());
        result.put("result", rows);
        return result;
    }

    /**
     * 根据文件路径获取该文件编码
     * @param fileName 文件路径
     * @return
     */
    public static String getFileEncoding(String fileName) {
        byte[] buf = new byte[4096];
        FileInputStream fis = null;
        String encoding = null;
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
            if (encoding != null) {
                logger.error("[" + fileName + "], detect encoding failed." );
                System.out.println("Detected encoding = " + encoding);
            } else {
                System.out.println("No encoding detected.");
            }

            // (5)
            detector.reset();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("[" + fileName + "], detect encoding failed." );
            return null;
        }
        return encoding;
    }

    /**
     * 根据文件对比对象，获取文本文件比对结果
     * @param fileCompareInfo
     * @return
     */
    public static Map<String, Object> compareTextFile(FileCompareInfo fileCompareInfo) {
        String sourceFile = fileCompareInfo.getSource();
        String targetFile = fileCompareInfo.getTarget();
        Map<String, Object> result = new HashMap<>();
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "<del>")
                .newTag(f -> "<strong>")
                .build();

        List<DiffRow> rows;
        try {
            String originalEncoding = getFileEncoding(sourceFile);
//            todo 无法解析的文件，直接采用utf8编码
            if(originalEncoding == null) {
                originalEncoding = "utf-8";
//                result.put("resultCode", -1);
//                result.put("errMsg", "[" + sourceFile+ "] encoding is not found, cannot compare file.");
//                return result;
            }
            String patchedEncoding = getFileEncoding(targetFile);
            if(patchedEncoding == null) {
                patchedEncoding = "utf-8";
//                result.put("resultCode", -1);
//                result.put("errMsg", "[" + targetFile+ "] encoding is not found, cannot compare file.");
//                return result;
            }
            List<String> original = Files.readAllLines(new File(sourceFile).toPath(), Charset.forName(originalEncoding));
            List<String> patched = Files.readAllLines(new File(targetFile).toPath(), Charset.forName(patchedEncoding));
            rows = generator.generateDiffRows(original, patched);
        } catch (IOException | DiffException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            result.put("resultCode", -1);
            result.put("errMsg", e.getMessage());
            return result;
        }

//        printTextCompareResult(rows);
        result.put("resultCode", 0);
        result.put("fileId", fileCompareInfo.getId());
        result.put("result", rows);
        result.put("type", 0);
        return result;
    }

    private static void printTextCompareResult(List<DiffRow> rows) {
        System.out.println("|line|original|new|");
        System.out.println("|----|--------|---|");
        int i = 0;
        for (DiffRow row : rows) {
            System.out.println("|" + ++i + "|" + row.getOldLine() + "|" + row.getNewLine() + "|");
        }
    }

    public static void main(String[] args){
//        FileInfo fileInfo = new FileInfo();
//        fileInfo.setSource("D:/QuickMDB_SYS_CC1.xml");
//        fileInfo.setTarget("D:/QuickMDB_SYS_CC.xml");
//        fileInfo.setCharsetName("utf-8");
//        compareTextFile(fileInfo);
//        demo5_1();
    }
}
