-- MariaDB 최적화된 스키마 (소문자, VARCHAR 크기 지정)
CREATE TABLE IF NOT EXISTS `permissions` (
                               `id`	int unsigned	NOT NULL AUTO_INCREMENT,
                               `resource`	varchar(100)	NULL,
                               `description`	varchar(255)	NULL,
                               `created_at`	timestamp	DEFAULT CURRENT_TIMESTAMP,
                               `updated_at`	timestamp	DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               `is_active`	boolean	DEFAULT TRUE,
                               `updated_by`	bigint unsigned	NULL,
                               `created_by`	bigint unsigned	NULL,
                               PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `organizations` (
                                 `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                                 `name`	varchar(150)	NULL,
                                 `domain_name`	varchar(100)	NULL,
                                 `created_at`	timestamp	DEFAULT CURRENT_TIMESTAMP,
                                 `updated_at`	timestamp	DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `roles` (
                         `id`    bigint unsigned    NOT NULL AUTO_INCREMENT,
                         `organization_id`  bigint unsigned NULL,
                         `name` varchar(100)   NULL,
                         `description`  varchar(500)   NULL,
                         PRIMARY KEY (`id`),
                         CONSTRAINT `fk_organizations_to_roles` FOREIGN KEY (`organization_id`)
                             REFERENCES `organizations` (`id`) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS `users` (
                         `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                         `name`	varchar(50)	NULL,
                         `email`	varchar(100)	NULL,
                         `password`	varchar(255)	NULL,
                         `status`	varchar(20)	NULL,
                         `created_at`	timestamp	DEFAULT CURRENT_TIMESTAMP,
                         `updated_at`	timestamp	DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `departments` (
                               `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                               `organization_id`	bigint unsigned	NOT NULL,
                               `name`	varchar(100)	NULL,
                               PRIMARY KEY (`id`),
                               CONSTRAINT `fk_organizations_to_departments` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`)
);

CREATE TABLE IF NOT EXISTS `positions` (
                             `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                             `organization_id`	bigint unsigned	NOT NULL,
                             `title`	varchar(100)	NULL,
                             PRIMARY KEY (`id`),
                             CONSTRAINT `fk_organizations_to_positions` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`)
);

CREATE TABLE IF NOT EXISTS `user_organizations` (
                                      `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                                      `user_id`	bigint unsigned	NOT NULL,
                                      `organization_id`	bigint unsigned	NOT NULL,
                                      `position_id`	bigint unsigned	NOT NULL,
                                      `department_id`	bigint unsigned	NOT NULL,
                                      `employee_number`	varchar(50)	NULL,
                                      `status`	varchar(20)	NULL,
                                      `created_at`	timestamp	DEFAULT CURRENT_TIMESTAMP,
                                      `updated_at`	timestamp	DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`id`),
                                      CONSTRAINT `fk_users_to_user_organizations` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                                      CONSTRAINT `fk_organizations_to_user_organizations` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`),
                                      CONSTRAINT `fk_positions_to_user_organizations` FOREIGN KEY (`position_id`) REFERENCES `positions` (`id`),
                                      CONSTRAINT `fk_departments_to_user_organizations` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
);

CREATE TABLE IF NOT EXISTS `role_permissions` (
                                    `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                                    `role_id`	bigint unsigned	NOT NULL,
                                    `permission_id`	int unsigned	NOT NULL,
                                    PRIMARY KEY (`id`),
                                    CONSTRAINT `fk_roles_to_role_permissions` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
                                    CONSTRAINT `fk_permissions_to_role_permissions` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
                                    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
);

CREATE TABLE IF NOT EXISTS `user_roles` (
                              `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                              `role_id`	bigint unsigned	NOT NULL,
                              `user_organization_id`	bigint unsigned	NOT NULL,
                              PRIMARY KEY (`id`),
                              CONSTRAINT `fk_roles_to_user_roles` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
                              CONSTRAINT `fk_user_organizations_to_user_roles` FOREIGN KEY (`user_organization_id`) REFERENCES `user_organizations` (`id`),
                              UNIQUE KEY `uk_user_role` (`role_id`, `user_organization_id`)
);

-- 성능 최적화를 위한 인덱스
CREATE INDEX IF NOT EXISTS
 `idx_users_email` ON `users` (`email`);
CREATE INDEX IF NOT EXISTS
 `idx_users_status` ON `users` (`status`);
CREATE INDEX IF NOT EXISTS
 `idx_user_organizations_user` ON `user_organizations` (`user_id`);
CREATE INDEX IF NOT EXISTS
 `idx_user_organizations_org` ON `user_organizations` (`organization_id`);
CREATE INDEX IF NOT EXISTS
 `idx_user_organizations_status` ON `user_organizations` (`status`);
CREATE INDEX IF NOT EXISTS
 `idx_roles_org` ON `roles` (`organization_id`);
CREATE INDEX IF NOT EXISTS
 `idx_permissions_resource` ON `permissions` (`resource`);
CREATE INDEX IF NOT EXISTS
 `idx_permissions_active` ON `permissions` (`is_active`);



CREATE TABLE IF NOT EXISTS `workflows` (
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

CREATE TABLE IF NOT EXISTS `schedules` (
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
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_schedules_to_workflows` FOREIGN KEY (`workflow_id`) REFERENCES `workflows` (`id`)
    );

CREATE TABLE IF NOT EXISTS `jobs` (
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

CREATE TABLE IF NOT EXISTS `tasks` (
                                       `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                       `name` varchar(100) NOT NULL UNIQUE,
    `type` varchar(50) NULL,
    `parameters` json NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `workflow_jobs` (
                                               `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                               `workflow_id` bigint unsigned NOT NULL,
                                               `job_id` bigint unsigned NOT NULL,
                                               PRIMARY KEY (`id`),
    CONSTRAINT `fk_workflow_jobs_to_workflows` FOREIGN KEY (`workflow_id`) REFERENCES `workflows` (`id`),
    CONSTRAINT `fk_workflow_jobs_to_jobs` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`),
    UNIQUE KEY `uk_workflow_job` (`workflow_id`, `job_id`)
    );

CREATE TABLE IF NOT EXISTS `job_tasks` (
                                           `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                           `job_id` bigint unsigned NOT NULL,
                                           `task_id` bigint unsigned NOT NULL,
                                           `execution_order` int NULL,
                                           PRIMARY KEY (`id`),
    CONSTRAINT `fk_job_tasks_to_jobs` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`),
    CONSTRAINT `fk_job_tasks_to_tasks` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
    UNIQUE KEY `uk_job_task` (`job_id`, `task_id`)
    );

CREATE TABLE IF NOT EXISTS `execution_logs` (
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

CREATE TABLE IF NOT EXISTS `configs` (
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

CREATE TABLE IF NOT EXISTS `categories` (
                                            `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                                            `name` varchar(100) NULL,
    `description` text NULL,
    `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    );

CREATE TABLE IF NOT EXISTS `user_configs` (
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
CREATE INDEX IF NOT EXISTS `idx_schedules_workflow` ON `schedules` (`workflow_id`);
CREATE INDEX IF NOT EXISTS `idx_jobs_enabled` ON `jobs` (`is_enabled`);
CREATE INDEX IF NOT EXISTS `idx_tasks_type` ON `tasks` (`type`);
CREATE INDEX IF NOT EXISTS `idx_workflows_enabled` ON `workflows` (`is_enabled`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_schedules_workflow` ON `schedules` (`workflow_id`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_job_name` ON `jobs` (`name`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_task_name` ON `tasks` (`name`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_workflows_name` ON `workflows` (`name`);
CREATE INDEX IF NOT EXISTS `idx_user_configs_user` ON `user_configs` (`user_id`);