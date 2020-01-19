package com.ztesoft.config.compare.controller;

import com.ztesoft.config.compare.entity.FileCollect;
import com.ztesoft.config.compare.entity.Project;
import com.ztesoft.config.compare.repository.FileCollectRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.repository.ProjectRepository;
import com.ztesoft.config.compare.service.FileCompareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author admin
 */
@RestController
@RequestMapping(value = "/collect")
public class CollectController {

    @Autowired
    private FileCompareService fileCompareService;

    @Autowired
    private FileCollectRepository fileCollectRepository;

    @Autowired
    private HostInfoRepository hostInfoRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @RequestMapping(value = "/project", method = RequestMethod.POST)
    public List<Map<String, Object>> collectFileByIds(@RequestBody List<Project> projectList) {
        List<Map<String, Object>> list = new ArrayList<>(projectList.size());
        for (Project project : projectList) {
            list.add(fileCompareService.collectFileByProjectId(project.getProjectId()));
        }
        return list;
    }

    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET)
    public List<Map<String, Object>> collectFileById(@PathVariable Long id) {
        fileCompareService.collectFileByProjectId(id);
        return null;
    }

    @RequestMapping(value = "/{projectId}/{hostId}", method = RequestMethod.GET)
    public List<FileCollect> getCollectFiles(@PathVariable("projectId") Long projectId, @PathVariable("hostId") Long hostId) {
        return fileCollectRepository.findByProjectIdAndHostId(projectId, hostId);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public Map<String, Object> getAllData() {
        Map<String, Object> map = new HashMap<>();
        map.put("file", fileCollectRepository.findAll());
        map.put("host", hostInfoRepository.findAll());
        map.put("project", projectRepository.findAll());
        return map;
    }
}
