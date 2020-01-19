package com.ztesoft.config.compare.repository;

import com.ztesoft.config.compare.entity.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    void deleteByProjectId(Long projectId);

    List<FileInfo> findByProjectId(Long projectId);

    FileInfo findByProjectIdAndGeneralPath(Long projectId, String path);

    FileInfo findByProjectIdAndGeneralPathAndFileIdNot(Long projectId, String path, Long fileId);

    boolean existsByProjectId(Long projectId);
}
