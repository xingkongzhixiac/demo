package com.example.demo.repository;

import com.example.demo.model.JobData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * JobData 数据仓库接口.
 *
 * <p>
 * 继承 JpaRepository 来获得标准的 CRUD, 分页和排序功能.
 * 继承 JpaSpecificationExecutor 来支持基于标准的动态查询 (dynamic criteria queries).
 * </p>
 */

public interface JobDataRepository extends JpaRepository<JobData, Long>, JpaSpecificationExecutor<JobData> {
}