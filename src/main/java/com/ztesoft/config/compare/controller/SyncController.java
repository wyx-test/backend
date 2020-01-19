package com.ztesoft.config.compare.controller;

import com.ztesoft.config.compare.dto.FileUriDto;
import com.ztesoft.config.compare.repository.FileInfoRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.service.FileCompareService;
import com.ztesoft.config.compare.service.FileSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/sync")
public class SyncController {

    @Autowired
    private HostInfoRepository hostInfoRepository;

    @Autowired
    private FileInfoRepository fileInfoRepository;
    @Autowired
    private FileCompareService fileCompareService;
    @Autowired
    private FileSyncService fileSyncService;

    private Map<String, Map<String, Object>> compareResult = new HashMap<>();

    @RequestMapping(value = "/file/{id}", method = RequestMethod.GET)
    public Map<String, Object> syncFile2Server(@PathVariable("id") Long id) {
        return fileSyncService.syncSingleFile(id);
    }

    @RequestMapping(value = "/files", method = RequestMethod.POST)
    public List<Map<String, Object>> syncFiles2Server(@RequestBody List<Long> ids) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long id : ids) {
            result.add(fileSyncService.syncSingleFile(id));
        }
        return result;
    }
}
