package com.ztesoft.config.compare.service.impl;

import com.ztesoft.config.compare.entity.Project;
import com.ztesoft.config.compare.repository.FileInfoRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.repository.ProjectRepository;
import com.ztesoft.config.compare.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private HostInfoRepository hostInfoRepository;

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Override
    public Map<String, Object> insert(Project project) {
        Map<String, Object> map = new HashMap<>();
        if (projectRepository.findByName(project.getName()) != null) {
            map.put("resultCode", -1);
            map.put("errMsg", "The project name is already existed.");
        } else {
            map.put("resultCode", 0);
            map.put("entity", projectRepository.save(project));
        }
        return map;
    }

    @Override
    public Map<String, Object> update(Project project) {
        List<Project> projectList = projectRepository.findByProjectIdNot(project.getProjectId());
        Map<String, Object> map = new HashMap<>();
        if (projectList != null && projectList.size() > 0) {
            for (Project p : projectList) {
                if (project.getName() != null && project.getName().equals(p.getName())) {
                    map.put("resultCode", -1);
                    map.put("errMsg", "The project name is already existed.");
                    return map;
                }
            }
        }
        map.put("resultCode", 0);
        map.put("entity", projectRepository.save(project));
        return map;
    }

    @Override
    @Transactional
    public Map<String, Object> delete(Long projectId) {
        Map<String, Object> map = new HashMap<>();
        hostInfoRepository.deleteByProjectId(projectId);
        fileInfoRepository.deleteByProjectId(projectId);
        projectRepository.deleteById(projectId);
        map.put("resultCode", projectRepository.existsById(projectId) ? -1 : 0);
        return map;
    }

    @Override
    public List<Project> queryAll() {
        return projectRepository.findAll();
    }
}
