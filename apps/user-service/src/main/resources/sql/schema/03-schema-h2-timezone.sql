-- ===================================================================
-- H2 전용 UTC Timezone 처리를 위한 스키마 수정 (v0.5)
-- ===================================================================
-- H2 데이터베이스는 MariaDB와 다른 문법을 사용하므로 별도 처리

-- 모든 timestamp 컬럼의 기본값 제거 (H2에서는 MODIFY COLUMN 문법이 다름)
-- H2에서는 ALTER TABLE table_name ALTER COLUMN column_name 문법 사용
-- H2 MariaDB 모드에서는 백틱으로 테이블명을 감싸야 함
SET TIME ZONE 'UTC';
ALTER TABLE `permission` ALTER COLUMN created_at SET DEFAULT NULL;
ALTER TABLE `permission` ALTER COLUMN updated_at SET DEFAULT NULL;

ALTER TABLE `organization` ALTER COLUMN created_at SET DEFAULT NULL;
ALTER TABLE `organization` ALTER COLUMN updated_at SET DEFAULT NULL;

ALTER TABLE `user` ALTER COLUMN created_at SET DEFAULT NULL;
ALTER TABLE `user` ALTER COLUMN updated_at SET DEFAULT NULL;
ALTER TABLE `user` ALTER COLUMN joined_at SET DEFAULT NULL;

ALTER TABLE `user_organization` ALTER COLUMN created_at SET DEFAULT NULL;
ALTER TABLE `user_organization` ALTER COLUMN updated_at SET DEFAULT NULL;

ALTER TABLE `workflow` ALTER COLUMN created_at SET DEFAULT NULL;
ALTER TABLE `workflow` ALTER COLUMN updated_at SET DEFAULT NULL;

ALTER TABLE `schedule` ALTER COLUMN last_run_at SET DEFAULT NULL;
ALTER TABLE `schedule` ALTER COLUMN created_at SET DEFAULT NULL;
ALTER TABLE `schedule` ALTER COLUMN updated_at SET DEFAULT NULL;

ALTER TABLE `job` ALTER COLUMN created_at SET DEFAULT NULL;
ALTER TABLE `job` ALTER COLUMN updated_at SET DEFAULT NULL;

ALTER TABLE `task` ALTER COLUMN created_at SET DEFAULT NULL;
ALTER TABLE `task` ALTER COLUMN updated_at SET DEFAULT NULL;

ALTER TABLE `execution_log` ALTER COLUMN executed_at SET DEFAULT NULL;
ALTER TABLE `execution_log` ALTER COLUMN reserved5 SET DEFAULT NULL;

ALTER TABLE `task_io_data` ALTER COLUMN created_at SET DEFAULT NULL;

-- config 테이블이 존재하는지 확인 후 ALTER 실행
-- ALTER TABLE `config` ALTER COLUMN created_at SET DEFAULT NULL;

ALTER TABLE `category` ALTER COLUMN created_at SET DEFAULT NULL;
ALTER TABLE `category` ALTER COLUMN updated_at SET DEFAULT NULL;

ALTER TABLE `user_config` ALTER COLUMN created_at SET DEFAULT NULL;
ALTER TABLE `user_config` ALTER COLUMN updated_at SET DEFAULT NULL;

-- 워크플로우 실행 테이블들 (기본값이 이미 NULL이므로 변경 불필요)
-- workflow_run, job_run, task_run 테이블은 이미 DEFAULT 값이 없음