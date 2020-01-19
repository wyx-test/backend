package com.ztesoft.config.compare.controller;

import com.ztesoft.config.compare.dto.HostInfoDto;
import com.ztesoft.config.compare.entity.*;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.service.HostService;
import com.ztesoft.config.compare.utils.HostUtil;
import com.ztesoft.config.compare.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/host")
public class HostController {

    @Autowired
    private HostInfoRepository hostInfoRepository;


    @Autowired
    private HostService hostService;

    @RequestMapping(method = RequestMethod.POST)
    public Map<String, Object> insert(@RequestBody HostInfoDto hostInfoDto) {
        return hostService.insert(hostInfoDto);
    }

    @Transactional
    @RequestMapping(method = RequestMethod.PUT)
    public Map<String, Object> update(@RequestBody HostInfoDto hostInfoDto) {
        return hostService.update(hostInfoDto);
    }

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> find() {
        List<HostInfo> hostInfos = hostInfoRepository.findAll();
        return ResponseUtil.renderTableResponse(hostInfos);
    }

    @RequestMapping(value = "/slave/{id}", method = RequestMethod.GET)
    public List<HostInfoDto> findSlaveById(@PathVariable Long id) {
        List<HostInfoDto> hostInfoDtos = hostService.findSlaveByProjectId(id);
        return hostInfoDtos;
    }

    @RequestMapping(value = "/master/{id}", method = RequestMethod.GET)
    public HostInfoDto findMasterById(@PathVariable Long id) {
        HostInfoDto hostInfoDto = hostService.findMasterByProjectId(id);
        return hostInfoDto;
    }

    @RequestMapping(value = "/4file/{id}", method = RequestMethod.GET)
    public List<HostDetail> findById(@PathVariable Long id) {
        return hostService.getHostDetailListById(id);
    }

    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET)
    public Map<String, Object> findByProjectId(@PathVariable Long id) {
        return ResponseUtil.renderTableResponse(hostService.findByProjectId(id));
    }


    @Transactional
    @RequestMapping(method = RequestMethod.DELETE)
    public Map<String, Object> delete(@RequestParam("hostId") Long hostId) {
        return hostService.delete(hostId);
    }

    @Transactional
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Map<String, Object> deleteById(@PathVariable("id") Long hostId) {
        return hostService.delete(hostId);
    }
}
