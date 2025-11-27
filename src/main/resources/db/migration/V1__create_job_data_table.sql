CREATE TABLE job_data (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          position_name VARCHAR(255),
                          work_location VARCHAR(255),
                          salary VARCHAR(100),
                          experience_requirement VARCHAR(100),
                          education_requirement VARCHAR(100),
                          position_tags TEXT,
                          company_name VARCHAR(255),
                          company_industry VARCHAR(255),
                          company_size VARCHAR(100),
                          financing_status VARCHAR(100)
);