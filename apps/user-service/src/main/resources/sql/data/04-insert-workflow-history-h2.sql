-- ===================================================================
-- 워크플로우 히스토리 테스트용 데이터 삽입 (H2 전용)
-- ===================================================================

-- 기존 실행 데이터 삭제 (참조 순서 고려)
DELETE FROM `task_run` WHERE id = 1;
DELETE FROM `job_run` WHERE id = 1;
DELETE FROM `workflow_run` WHERE id = 1;

-- AUTO_INCREMENT 초기화
ALTER TABLE `task_run` AUTO_INCREMENT = 1;
ALTER TABLE `job_run` AUTO_INCREMENT = 1;
ALTER TABLE `workflow_run` AUTO_INCREMENT = 1;

-- 워크플로우 실행 데이터 삽입 (workflow_run)
INSERT INTO `workflow_run` (
    `workflow_id`,
    `trace_id`,
    `run_number`,
    `status`,
    `trigger_type`,
    `started_at`,
    `finished_at`,
    `created_by`
) VALUES (
             1,
             '3e3c832d-b51f-48ea-95f9-98f0ae6d3413',
             NULL,
             'FAILED',
             NULL,
             '2025-09-22 18:18:43',
             '2025-09-22 18:18:44',
             NULL
         );

-- Job 실행 데이터 삽입 (job_run) - H2에서는 NOW() 사용
INSERT INTO `job_run` (
    `id`,
    `workflow_run_id`,
    `job_id`,
    `status`,
    `execution_order`,
    `started_at`,
    `finished_at`,
    `created_at`
) VALUES (
             1,
             1,
             1,
             'FAILED',
             NULL,
             '2025-09-22 18:18:44',
             '2025-09-22 18:18:44',
             NOW()
         );

-- Task 실행 데이터 삽입 (task_run) - H2에서는 NOW() 사용
INSERT INTO `task_run` (
    `id`,
    `job_run_id`,
    `task_id`,
    `status`,
    `execution_order`,
    `started_at`,
    `finished_at`,
    `created_at`
) VALUES (
             1,
             1,
             1,
             'FAILED',
             NULL,
             '2025-09-22 18:18:44',
             '2025-09-22 18:18:44',
             NOW()
         );