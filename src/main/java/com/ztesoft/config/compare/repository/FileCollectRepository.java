package com.ztesoft.config.compare.repository;

import com.ztesoft.config.compare.entity.FileCollect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FileCollectRepository extends JpaRepository<FileCollect, Long> {
    void deleteByFileIdAndHostId(Long fileId, Long hostId);

    @Transactional
    void deleteByHostId(Long hostId);

    List<FileCollect> findByProjectIdAndHostId(Long projectId, Long hostId);

    FileCollect findByHostIdAndFileId(Long hostId, Long fileId);
}
