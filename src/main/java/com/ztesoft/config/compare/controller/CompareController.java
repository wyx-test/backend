package com.ztesoft.config.compare.controller;

import com.ztesoft.config.compare.dto.CompareReport;
import com.ztesoft.config.compare.dto.FileUriDto;
import com.ztesoft.config.compare.entity.*;
import com.ztesoft.config.compare.repository.FileInfoRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.service.FileCompareService;
import com.ztesoft.config.compare.service.ProjectService;
import com.ztesoft.config.compare.utils.CSVUtil;
import com.ztesoft.config.compare.utils.HostUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.*;

@RestController
@RequestMapping(value = "/compare")
public class CompareController {

    @Autowired
    private HostInfoRepository hostInfoRepository;

    @Autowired
    private FileInfoRepository fileInfoRepository;
    @Autowired
    private FileCompareService fileCompareService;

    private Map<String, Map<String, Object>> compareResult = new HashMap<>();


    /**
     * 获取所有的服务器及服务器下的配置文件列表
     *
     * @return
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Map<String, Object>> getHost() {
        List<HostInfo> hostInfos = hostInfoRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>(hostInfos.size());
        return result;
    }

    @RequestMapping(value = "/collect/{id}", method = RequestMethod.GET)
    public Map<String, Object> collectFile(@PathVariable Long id) {
        return fileCompareService.collectFileByProjectId(id);
    }

    @RequestMapping(value = "/execute/project/{id}", method = RequestMethod.GET)
    public Map<String, Map<String, Object>> compareFile(@PathVariable Long id) {
        compareResult.put(id.toString(), fileCompareService.compareFileByProject(id));
        return compareResult;
    }

    @RequestMapping(value = "/execute/file/{id}", method = RequestMethod.GET)
    public Map<String, Object> compareFileById(@PathVariable Long id) {
        Map<String, Object> result = fileCompareService.compareFileByFileCollect(id);
        compareResult.put("file" + id, result);
        return result;
    }

    @RequestMapping(value = "/execute/file", method = RequestMethod.POST)
    public Map<String, Object> compareFileById(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            compareResult.put("file" + id, fileCompareService.compareFileByFileCollect(id));
        }
        return null;
    }

    @RequestMapping(value = "/result1/{id}", method = RequestMethod.GET)
    public Object getResult(@PathVariable Long id) {
        return compareResult.get("file" + id);
    }

    @RequestMapping(value = "/sync", method = RequestMethod.POST)
    public List<Map<String, Object>> syncFile2Server(@RequestBody List<Project> projectList) {
        List<Map<String, Object>> list = new ArrayList<>(projectList.size());
        for (Project project : projectList) {
            Long id = project.getProjectId();
            list.add(fileCompareService.syncFile2Server(id));
        }
        return list;
    }

    @RequestMapping(value = "/report", method = RequestMethod.GET)
    public Map<String, Object> getReport() {
        Map<String, Object> map = new HashMap<>();
        if(compareResult.size() == 0) {
            map.put("resultCode",-1);
            map.put("errMsg", "No compare result found, please compare the file first.");
            return map;
        }
//        resultCode errMsg filePath
        List<CompareReport> compareReports = fileCompareService.getReportFromCompareResult(compareResult);

        Collections.sort(compareReports);
        String filePath = CSVUtil.generateReportFile(compareReports);
        if (filePath == null) {
            map.put("resultCode",-1);
            map.put("errMsg", "generate csv file failed");
            return map;
        }
        map.put("resultCode",0);
        map.put("filePath", filePath);
        return map;
    }
}
