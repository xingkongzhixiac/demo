package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "job_data")
@Getter
@Setter
public class JobData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position_name")
    private String positionName;

    @Column(name = "work_location")
    private String workLocation;

    @Column(name = "salary")
    private String salary;

    @Column(name = "experience_requirement")
    private String experienceRequirement;

    @Column(name = "education_requirement")
    private String educationRequirement;

    // --- 修改点在这里 ---
    @Lob
    @Column(name = "position_tags", columnDefinition = "TEXT") // 明确指定列类型为TEXT
    private String positionTags;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_industry")
    private String companyIndustry;

    @Column(name = "company_size")
    private String companySize;

    @Column(name = "financing_status")
    private String financingStatus;
}