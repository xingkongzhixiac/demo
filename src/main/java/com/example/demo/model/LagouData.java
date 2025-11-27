package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lagou_data")
@Getter
@Setter
public class LagouData {

    @Id
    private Long id;

    private String category;

    @Column(name = "big_category")
    private String bigCategory;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "position_name")
    private String positionName;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_full_name")
    private String companyFullName;

    @Column(name = "company_size")
    private String companySize;

    // --- 修改点 ---
    @Lob
    @Column(name = "industry_field", columnDefinition = "TEXT")
    private String industryField;

    @Column(name = "finance_stage")
    private String financeStage;

    // --- 修改点 ---
    @Lob
    @Column(name = "position_type", columnDefinition = "TEXT")
    private String positionType;

    @Column(name = "create_time")
    private String createTime;

    private String city;

    private String district;

    private String salary;

    @Column(name = "work_year")
    private String workYear;

    @Column(name = "job_nature")
    private String jobNature;

    private String education;

    // --- 修改点 ---
    @Lob
    @Column(name = "position_detail", columnDefinition = "TEXT")
    private String positionDetail;

    // --- 修改点 ---
    @Lob
    @Column(name = "position_advantage", columnDefinition = "TEXT")
    private String positionAdvantage;

    @Column(name = "created_at")
    private String createdAt;
}