package com.ztesoft.config.compare.service.impl;

import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.repository.FileInfoRepository;
import com.ztesoft.config.compare.service.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileInfoServiceImpl implements FileInfoService {
    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Override
    public Map<String, Object> insert(FileInfo fileInfo) {
        Map<String, Object> map = new HashMap<>();
//        if (fileInfoRepository.findByProjectIdAndFileName(fileInfo.getProjectId(), fileInfo.getFileName()) != null) {
//            map.put("resultCode", -1);
//            map.put("errMsg", "File name is already existed.");
//            return map;
//        }
        if (fileInfoRepository.findByProjectIdAndGeneralPath(fileInfo.getProjectId(), fileInfo.getGeneralPath()) != null) {
            map.put("resultCode", -1);
            map.put("errMsg", "Default path is already existed.");
            return map;
        }
        map.put("resultCode", 0);
        map.put("fileInfo", fileInfoRepository.save(fileInfo));
        return map;
    }

    @Override
    public Map<String, Object> update(FileInfo fileInfo) {
        Map<String, Object> map = new HashMap<>();
//        if (fileInfoRepository.findByProjectIdAndFileNameAndFileIdNot(fileInfo.getProjectId(), fileInfo.getFileName(), fileInfo.getFileId()) != null) {
//            map.put("resultCode", -1);
//            map.put("errMsg", "File name is already existed.");
//            return map;
//        }
        if (fileInfoRepository.findByProjectIdAndGeneralPathAndFileIdNot(fileInfo.getProjectId(), fileInfo.getGeneralPath(), fileInfo.getFileId()) != null) {
            map.put("resultCode", -1);
            map.put("errMsg", "Default path is already existed.");
            return map;
        }
        map.put("resultCode", 0);
        map.put("fileInfo", fileInfoRepository.save(fileInfo));
        return map;
    }

    @Override
    public Map<String, Object> delete(Long fileId) {
        Map<String, Object> map = new HashMap<>();
        fileInfoRepository.deleteById(fileId);
        boolean existed = fileInfoRepository.existsById(fileId);
        map.put("resultCode", existed ? -1 : 0);
        if (existed) {
            map.put("errMsg", "Delete file failed.");
        }
        return map;
    }

    @Override
    public boolean deleteByProjectId(Long projectId) {
        fileInfoRepository.deleteByProjectId(projectId);
        return !fileInfoRepository.existsByProjectId(projectId);
    }

    @Override
    public List<FileInfo> findAll() {
        return fileInfoRepository.findAll();
    }

    @Override
    public List<FileInfo> findByProjectId(Long projectId) {
        return fileInfoRepository.findByProjectId(projectId);
    }
}
