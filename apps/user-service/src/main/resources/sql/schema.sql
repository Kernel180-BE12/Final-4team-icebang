-- MariaDB 최적화된 스키마 (단수형 테이블 네이밍, 외래 키 제약조건 제거 버전)

CREATE TABLE IF NOT EXISTS `permission` (
                                            `id` int unsigned NOT NULL AUTO_INCREMENT,
                                            `resource` varchar(100) NULL,
    `description` varchar(255) NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_active` boolean DEFAULT TRUE,
    `updated_by` bigint unsigned NULL,
    `created_by` bigint unsigned NULL,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `organization` (
                                              `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                              `name` varchar(150) NULL,
    `domain_name` varchar(100) NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `role` (
                                      `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                      `organization_id` bigint unsigned NULL,
                                      `name` varchar(100) NULL,
    `description` varchar(500) NULL,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `user` (
                                      `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                      `name` varchar(50) NULL,
    `email` varchar(100) NULL,
    `password` varchar(255) NULL,
    `status` varchar(20) NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `department` (
                                            `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                            `organization_id` bigint unsigned NOT NULL,
                                            `name` varchar(100) NULL,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `position` (
                                          `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                          `organization_id` bigint unsigned NOT NULL,
                                          `title` varchar(100) NULL,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `user_organization` (
                                                   `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                                   `user_id` bigint unsigned NOT NULL,
                                                   `organization_id` bigint unsigned NOT NULL,
                                                   `position_id` bigint unsigned NOT NULL,
                                                   `department_id` bigint unsigned NOT NULL,
                                                   `employee_number` varchar(50) NULL,
    `status` varchar(20) NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `role_permission` (
                                                 `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                                 `role_id` bigint unsigned NOT NULL,
                                                 `permission_id` int unsigned NOT NULL,
                                                 PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
    );

CREATE TABLE IF NOT EXISTS `user_role` (
                                           `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                           `role_id` bigint unsigned NOT NULL,
                                           `user_organization_id` bigint unsigned NOT NULL,
                                           PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`role_id`, `user_organization_id`)
    );

-- 성능 최적화를 위한 인덱스
CREATE INDEX IF NOT EXISTS `idx_user_email` ON `user` (`email`);
CREATE INDEX IF NOT EXISTS `idx_user_status` ON `user` (`status`);
CREATE INDEX IF NOT EXISTS `idx_user_organization_user` ON `user_organization` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_user_organization_org` ON `user_organization` (`organization_id`);
CREATE INDEX IF NOT EXISTS `idx_user_organization_status` ON `user_organization` (`status`);
CREATE INDEX IF NOT EXISTS `idx_role_org` ON `role` (`organization_id`);
CREATE INDEX IF NOT EXISTS `idx_permission_resource` ON `permission` (`resource`);
CREATE INDEX IF NOT EXISTS `idx_permission_active` ON `permission` (`is_active`);



CREATE TABLE IF NOT EXISTS `workflow` (
                                          `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                          `name` varchar(100) NOT NULL UNIQUE,
    `description` text NULL,
    `is_enabled` boolean DEFAULT TRUE,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `created_by` bigint unsigned NULL,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by` bigint unsigned NULL,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `schedule` (
                                          `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                          `workflow_id` bigint unsigned NOT NULL,
                                          `cron_expression` varchar(50) NULL,
    `parameters` json NULL,
    `is_active` boolean DEFAULT TRUE,
    `last_run_status` varchar(20) NULL,
    `last_run_at` timestamp NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `created_by` bigint unsigned NULL,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by` bigint unsigned NULL,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `job` (
                                     `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                     `name` varchar(100) NOT NULL UNIQUE,
    `description` text NULL,
    `is_enabled` boolean DEFAULT TRUE,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `created_by` bigint unsigned NULL,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by` bigint unsigned NULL,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `task` (
                                      `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                      `name` varchar(100) NOT NULL UNIQUE,
    `type` varchar(50) NULL,
    `parameters` json NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `workflow_job` (
                                              `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                              `workflow_id` bigint unsigned NOT NULL,
                                              `job_id` bigint unsigned NOT NULL,
                                              PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_job` (`workflow_id`, `job_id`)
    );

CREATE TABLE IF NOT EXISTS `job_task` (
                                          `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                          `job_id` bigint unsigned NOT NULL,
                                          `task_id` bigint unsigned NOT NULL,
                                          `execution_order` int NULL,
                                          PRIMARY KEY (`id`),
    UNIQUE KEY `uk_job_task` (`job_id`, `task_id`)
    );

CREATE TABLE IF NOT EXISTS `execution_log` (
                                               `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                               `execution_type` varchar(20) NULL COMMENT 'task, schedule, job, workflow',
    `source_id` bigint unsigned NULL COMMENT '모든 데이터에 대한 ID ex: job_id, schedule_id, task_id, ...',
    `log_level` varchar(20) NULL,
    `executed_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `log_message` text NULL,
    `trace_id` char(36) NULL,
    `config_snapshot` json NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_source_id_type` (`source_id`, `execution_type`)
    );

CREATE TABLE IF NOT EXISTS `task_io_data` (
                                              `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                              `trace_id` char(36) NULL,
    `io_type` varchar(10) NULL COMMENT 'INPUT, OUTPUT',
    `name` varchar(100) NULL,
    `data_type` varchar(50) NULL,
    `data_value` json NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_trace_id` (`trace_id`)
    );

CREATE TABLE IF NOT EXISTS `config` (
                                        `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                        `target_type` varchar(50) NULL COMMENT 'user, job, workflow',
    `target_id` bigint unsigned NULL,
    `version` int NULL,
    `json` json NULL,
    `is_active` boolean DEFAULT TRUE,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `created_by` bigint unsigned NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_target` (`target_type`, `target_id`)
    );

CREATE TABLE IF NOT EXISTS `category` (
                                          `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                          `name` varchar(100) NULL,
    `description` text NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `user_config` (
                                             `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                             `user_id` bigint unsigned NOT NULL,
                                             `type` varchar(50) NULL,
    `name` varchar(100) NULL,
    `json` json NULL,
    `is_active` boolean DEFAULT TRUE,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    );

-- 인덱스 추가 (성능 최적화)
CREATE INDEX IF NOT EXISTS `idx_schedule_workflow` ON `schedule` (`workflow_id`);
CREATE INDEX IF NOT EXISTS `idx_job_enabled` ON `job` (`is_enabled`);
CREATE INDEX IF NOT EXISTS `idx_task_type` ON `task` (`type`);
CREATE INDEX IF NOT EXISTS `idx_workflow_enabled` ON `workflow` (`is_enabled`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_schedule_workflow` ON `schedule` (`workflow_id`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_job_name` ON `job` (`name`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_task_name` ON `task` (`name`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_workflow_name` ON `workflow` (`name`);
CREATE INDEX IF NOT EXISTS `idx_user_config_user` ON `user_config` (`user_id`);