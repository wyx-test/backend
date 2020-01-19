package com.ztesoft.config.compare.controller;

import com.ztesoft.config.compare.entity.Project;
import com.ztesoft.config.compare.repository.ProjectRepository;
import com.ztesoft.config.compare.service.ProjectService;
import com.ztesoft.config.compare.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/project")
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @RequestMapping(method = RequestMethod.POST)
    public Map<String, Object> insert(@RequestBody Project project) {
        return projectService.insert(project);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Map<String, Object> update(@RequestBody Project project) {
        return projectService.update(project);
    }

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> find() {
        List<Project> projects = projectRepository.findAll();
        return ResponseUtil.renderTableResponse(projects);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Project findById(@PathVariable Long id) {
        return projectRepository.getOne(id);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public Map<String, Object> delete(@RequestParam("projectId") Long projectId) {
        return projectService.delete(projectId);
    }
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Map<String, Object> deleteById(@PathVariable Long id) {
        return projectService.delete(id);
    }
}
