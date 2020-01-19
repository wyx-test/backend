package com.ztesoft.config.compare.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="project",
        uniqueConstraints = {@UniqueConstraint(columnNames="name")})
public class Project {
    @Id
    @GeneratedValue
    private Long projectId;
    private String name;
    private String comments;
    private Long masterHostId;
}
