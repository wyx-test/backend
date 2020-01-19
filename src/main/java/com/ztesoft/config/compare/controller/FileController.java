package com.ztesoft.config.compare.controller;

import com.alibaba.fastjson.JSON;
import com.ztesoft.config.compare.dto.DeltaFileInfo;
import com.ztesoft.config.compare.dto.FileInfoDto;
import com.ztesoft.config.compare.dto.SpecialValue;
import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.service.FileInfoService;
import com.ztesoft.config.compare.utils.ResponseUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/file")
public class FileController {

    @Autowired
    private FileInfoService fileInfoService;

    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET)
    public Map<String, Object> findByProjectId(@PathVariable Long id) {
        List<FileInfo> fileInfos = fileInfoService.findByProjectId(id);
        List<FileInfoDto> fileInfoDtos = new ArrayList<>(fileInfos.size());
        for(FileInfo fileInfo:fileInfos) {
            FileInfoDto fileInfoDto = new FileInfoDto();
            BeanUtils.copyProperties(fileInfo,fileInfoDto);
            String valueMapStr = fileInfo.getSpecialValue();
            if(valueMapStr.startsWith("[{") && valueMapStr.endsWith("}]")) {
                fileInfoDto.setSpecialValues(JSON.parseArray(valueMapStr, SpecialValue.class));
            }
            String deltaFileStr = fileInfo.getDeltaFileInfos();
            if(deltaFileStr.startsWith("[{") && deltaFileStr.endsWith("}]")) {
                fileInfoDto.setDeltaFileInfos(JSON.parseArray(deltaFileStr, DeltaFileInfo.class));
            }
            fileInfoDtos.add(fileInfoDto);
        }
        return ResponseUtil.renderTableResponse(fileInfoDtos);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Map<String,Object> insert(@RequestBody FileInfoDto fileInfoDto) {
        List<SpecialValue> valueList = fileInfoDto.getSpecialValues();
        List<DeltaFileInfo> deltaFileInfos = fileInfoDto.getDeltaFileInfos();
//      删除字符串最后的文件分隔符
        FileInfo fileInfo = new FileInfo();
        BeanUtils.copyProperties(fileInfoDto,fileInfo);
        fileInfo.setSpecialValue(JSON.toJSONString(valueList));
        fileInfo.setDeltaFileInfos(JSON.toJSONString(deltaFileInfos));
        return fileInfoService.insert(fileInfo);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Map<String,Object> update(@RequestBody  FileInfoDto fileInfoDto) {
        List<SpecialValue> valueList = fileInfoDto.getSpecialValues();
        List<DeltaFileInfo> deltaFileInfos = fileInfoDto.getDeltaFileInfos();
//      删除字符串最后的文件分隔符
        FileInfo fileInfo = new FileInfo();
        BeanUtils.copyProperties(fileInfoDto,fileInfo);
        fileInfo.setSpecialValue(JSON.toJSONString(valueList));
        fileInfo.setDeltaFileInfos(JSON.toJSONString(deltaFileInfos));
        return fileInfoService.update(fileInfo);
    }

    @RequestMapping(method = RequestMethod.DELETE,value = "/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        return fileInfoService.delete(id);
    }
}
