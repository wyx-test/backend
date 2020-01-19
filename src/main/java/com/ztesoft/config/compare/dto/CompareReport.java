package com.ztesoft.config.compare.dto;

import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostInfo;
import com.ztesoft.config.compare.entity.Project;
import lombok.Data;

@Data
public class CompareReport implements Comparable<CompareReport> {
    private Project project;
    private HostInfo hostInfo;
    private HostInfo masterHostInfo;
    private Integer resultCode;
    private String message;
    private Boolean compareResult;
    private FileInfo fileInfo;
    private Integer totalCount;
    private Integer passed;
    private Integer notPassed;

    @Override
    public int compareTo(CompareReport o) {
        int flag = this.project.getName().compareTo(o.getProject().getName());
        if (flag == 0) {
            flag = this.fileInfo.getFileName().compareTo(o.getFileInfo().getFileName());
        }
        return flag;
    }

}
