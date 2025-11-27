package com.example.demo.config;

import com.example.demo.model.JobData;
import com.example.demo.model.LagouData;
import com.example.demo.repository.JobDataRepository;
import com.example.demo.repository.LagouDataRepository;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    // 批量大小：每1000条提交一次
    private static final int BATCH_SIZE = 1000;

    private final JobDataRepository jobDataRepository;
    private final LagouDataRepository lagouDataRepository;

    public DataInitializer(JobDataRepository jobDataRepository, LagouDataRepository lagouDataRepository) {
        this.jobDataRepository = jobDataRepository;
        this.lagouDataRepository = lagouDataRepository;
    }

    @Override
    public void run(String... args) {
        long startTime = System.currentTimeMillis();
        loadJobDataFromCsv();
        loadLagouDataFromCsv();
        long endTime = System.currentTimeMillis();
        logger.info("Total data import time: {} ms", (endTime - startTime));
    }

    private void loadJobDataFromCsv() {
        if (jobDataRepository.count() > 0) {
            logger.info("JobData table already has data. Skipping.");
            return;
        }

        logger.info("Starting batch import for JobData...");
        List<JobData> batchList = new ArrayList<>(BATCH_SIZE);

        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource("data/JobData.csv").getInputStream(), StandardCharsets.UTF_8))) {
            reader.readNext(); // 跳过表头
            String[] line;
            int totalCount = 0;

            while ((line = reader.readNext()) != null) {
                try {
                    if (line.length < 10) continue;

                    JobData jobData = new JobData();
                    jobData.setPositionName(line[0]);
                    jobData.setWorkLocation(line[1]);
                    jobData.setSalary(line[2]);
                    jobData.setExperienceRequirement(line[3]);
                    jobData.setEducationRequirement(line[4]);
                    jobData.setPositionTags(line[5]);
                    jobData.setCompanyName(line[6]);
                    jobData.setCompanyIndustry(line[7]);
                    jobData.setCompanySize(line[8]);
                    jobData.setFinancingStatus(line[9]);

                    batchList.add(jobData);
                    totalCount++;

                    // 缓冲区达到 BATCH_SIZE，执行一次批量保存
                    if (batchList.size() >= BATCH_SIZE) {
                        jobDataRepository.saveAll(batchList);
                        batchList.clear(); // 清空缓冲区
                        logger.info("Saved {} records to JobData...", totalCount);
                    }
                } catch (Exception e) {
                    logger.error("Skipping invalid JobData row", e);
                }
            }

            // 处理剩余的数据
            if (!batchList.isEmpty()) {
                jobDataRepository.saveAll(batchList);
            }
            logger.info("JobData import completed. Total records: {}", totalCount);

        } catch (Exception e) {
            logger.error("Critical error importing JobData", e);
        }
    }

    private void loadLagouDataFromCsv() {
        if (lagouDataRepository.count() > 0) {
            logger.info("LagouData table already has data. Skipping.");
            return;
        }

        logger.info("Starting batch import for LagouData...");
        List<LagouData> batchList = new ArrayList<>(BATCH_SIZE);

        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource("data/Lagou.csv").getInputStream(), StandardCharsets.UTF_8))) {
            reader.readNext(); // 跳过表头
            String[] line;
            int totalCount = 0;

            while ((line = reader.readNext()) != null) {
                try {
                    if (line.length < 21) continue;

                    LagouData lagouData = new LagouData();
                    lagouData.setId(safeParseLong(line[0]));
                    lagouData.setCategory(line[1]);
                    lagouData.setBigCategory(line[2]);
                    lagouData.setPositionId(safeParseLong(line[3]));
                    lagouData.setPositionName(line[4]);
                    lagouData.setCompanyId(safeParseLong(line[5]));
                    lagouData.setCompanyFullName(line[6]);
                    lagouData.setCompanySize(line[7]);
                    lagouData.setIndustryField(line[8]);
                    lagouData.setFinanceStage(line[9]);
                    lagouData.setPositionType(line[10]);
                    lagouData.setCreateTime(line[11]);
                    lagouData.setCity(line[12]);
                    lagouData.setDistrict(line[13]);
                    lagouData.setSalary(line[14]);
                    lagouData.setWorkYear(line[15]);
                    lagouData.setJobNature(line[16]);
                    lagouData.setEducation(line[17]);
                    lagouData.setPositionDetail(line[18]);
                    lagouData.setPositionAdvantage(line[19]);
                    lagouData.setCreatedAt(line[20]);

                    batchList.add(lagouData);
                    totalCount++;

                    if (batchList.size() >= BATCH_SIZE) {
                        lagouDataRepository.saveAll(batchList);
                        batchList.clear();
                        logger.info("Saved {} records to LagouData...", totalCount);
                    }
                } catch (Exception e) {
                    logger.error("Skipping invalid LagouData row ID: " + (line.length > 0 ? line[0] : "?"), e);
                }
            }

            if (!batchList.isEmpty()) {
                lagouDataRepository.saveAll(batchList);
            }
            logger.info("LagouData import completed. Total records: {}", totalCount);

        } catch (Exception e) {
            logger.error("Critical error importing LagouData", e);
        }
    }

    private Long safeParseLong(String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            return 0L;
        }
        try {
            return Long.parseLong(value.replaceAll("[\"']", "").trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}