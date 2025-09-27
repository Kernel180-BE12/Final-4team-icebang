-- ===================================================================
-- MariaDB 전용 UTC Timezone 처리를 위한 스키마 수정 (v0.5)
-- ===================================================================
-- MariaDB에서는 UTC_TIMESTAMP() 함수를 사용할 수 있지만,
-- 애플리케이션에서 Instant로 처리하므로 기본값을 제거

-- 모든 timestamp 컬럼의 기본값을 UTC 기준으로 변경
ALTER TABLE `permission` MODIFY COLUMN created_at timestamp NULL;
ALTER TABLE `permission` MODIFY COLUMN updated_at timestamp NULL;

ALTER TABLE `organization` MODIFY COLUMN created_at timestamp NULL;
ALTER TABLE `organization` MODIFY COLUMN updated_at timestamp NULL;

ALTER TABLE `user` MODIFY COLUMN created_at timestamp NULL;
ALTER TABLE `user` MODIFY COLUMN updated_at timestamp NULL;
ALTER TABLE `user` MODIFY COLUMN joined_at timestamp NULL;

ALTER TABLE `user_organization` MODIFY COLUMN created_at timestamp NULL;
ALTER TABLE `user_organization` MODIFY COLUMN updated_at timestamp NULL;

ALTER TABLE `workflow` MODIFY COLUMN created_at timestamp NULL;
ALTER TABLE `workflow` MODIFY COLUMN updated_at timestamp NULL;

ALTER TABLE `schedule` MODIFY COLUMN last_run_at timestamp NULL;
ALTER TABLE `schedule` MODIFY COLUMN created_at timestamp NULL;
ALTER TABLE `schedule` MODIFY COLUMN updated_at timestamp NULL;

ALTER TABLE `job` MODIFY COLUMN created_at timestamp NULL;
ALTER TABLE `job` MODIFY COLUMN updated_at timestamp NULL;

ALTER TABLE `task` MODIFY COLUMN created_at timestamp NULL;
ALTER TABLE `task` MODIFY COLUMN updated_at timestamp NULL;

ALTER TABLE `execution_log` MODIFY COLUMN executed_at timestamp NULL;
ALTER TABLE `execution_log` MODIFY COLUMN reserved5 timestamp NULL;

ALTER TABLE `task_io_data` MODIFY COLUMN created_at timestamp NULL;

-- config 테이블이 존재하지 않아 ALTER 실행 불가
-- ALTER TABLE `config` MODIFY COLUMN created_at timestamp NULL;

ALTER TABLE `category` MODIFY COLUMN created_at timestamp NULL;
ALTER TABLE `category` MODIFY COLUMN updated_at timestamp NULL;

ALTER TABLE `user_config` MODIFY COLUMN created_at timestamp NULL;
ALTER TABLE `user_config` MODIFY COLUMN updated_at timestamp NULL;

-- 워크플로우 실행 테이블 (이미 DEFAULT 값이 없으므로 변경 불필요)
-- workflow_run, job_run, task_run 테이블들은 기본값이 이미 적절히 설정됨