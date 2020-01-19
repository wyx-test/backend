package com.ztesoft.config.compare.service;

import com.ztesoft.config.compare.entity.Project;

import java.util.List;
import java.util.Map;

/**
 * project服务接口
 */
public interface ProjectService {

    Map<String,Object> insert(Project project);

    Map<String,Object> update(Project project);

    Map<String,Object> delete(Long projectId);

    List<Project> queryAll();

}
