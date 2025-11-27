# 项目概览

这是一个使用 Spring Boot、JPA（Hibernate）和 SQL 的后端服务项目，构建工具为 Maven。适用于在 Windows 下通过 IntelliJ IDEA 进行开发与调试。

## 先决条件

- Java 17+（或项目 `pom.xml` 指定的版本）
- Maven 或项目自带的 `mvnw.cmd`
- 数据库（MySQL / PostgreSQL / H2 等）
- IntelliJ IDEA 2025.1.x（推荐）

## 快速开始（Windows）

1. 克隆仓库并进入目录：
   - `git clone <repo-url>`
   - `cd <project-dir>`

2. 配置数据库连接：编辑 `src/main/resources/application.properties` 或 `application.yml`，示例（MySQL）：

3. 构建并运行：
   - 使用项目包装器： `mvnw.cmd clean package` 然后 `mvnw.cmd spring-boot:run`
   - 或使用本地 Maven： `mvn clean package` 然后 `mvn spring-boot:run`

4. 运行测试：
   - `mvnw.cmd test` 或 `mvn test`

## 在 IntelliJ IDEA 中打开

- 选择 `File > Open`，打开包含 `pom.xml` 的项目根目录，IDE 会自动以 Maven 项目导入依赖。
- 已启用 JPA Facet（见 `demo.iml`），确保 `Entity` 路径在 `Project Structure` 中被包含。
- 可以直接运行 Application 主类或使用运行配置运行单元测试。

## 数据库迁移（可选）

建议使用 `Flyway` 或 `Liquibase` 管理 schema 变更。把迁移脚本放在 `src/main/resources/db/migration`（Flyway）或相应目录。

## 配置说明

- `spring.jpa.hibernate.ddl-auto`：
  - `validate`：校验映射，不修改数据库（生产推荐）
  - `update`：自动更新表结构（开发可用）
  - `create` / `create-drop`：创建并在退出时删除（仅测试）
- 日志级别可在 `application.properties` 中设置：
## 常见问题

- 启动失败提示找不到数据源：确认 `spring.datasource.*` 已正确配置，数据库服务已启动且网络可达。
- JPA 报错实体找不到或表结构不匹配：检查实体类是否在 Spring Boot 扫描路径下，或将 `spring.jpa.hibernate.ddl-auto` 临时设为 `update` 以调试。

## 项目结构（简要）

- `src/main/java` — 应用源码
- `src/main/resources` — 配置与静态资源（包含 `application.properties`）
- `src/test/java` — 单元与集成测试
- `pom.xml` — Maven 配置

## 联系与贡献

使用 Git 分支与 PR 流程提交变更。遵循代码风格与单元测试覆盖要求。
