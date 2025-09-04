-- MariaDB 최적화된 스키마 (소문자, VARCHAR 크기 지정)
CREATE TABLE `permissions` (
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

CREATE TABLE `organizations` (
                                 `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                                 `name`	varchar(150)	NULL,
                                 `domain_name`	varchar(100)	NULL,
                                 `created_at`	timestamp	DEFAULT CURRENT_TIMESTAMP,
                                 `updated_at`	timestamp	DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`)
);

CREATE TABLE `roles` (
                         `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                         `organization_id`	bigint unsigned NULL,
                         `name`	varchar(100)	NULL,
                         `description`	varchar(500)	NULL,
                         PRIMARY KEY (`id`),
                         CONSTRAINT `fk_organizations_to_roles` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`)
);

CREATE TABLE `users` (
                         `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                         `name`	varchar(50)	NULL,
                         `email`	varchar(100)	NULL,
                         `password`	varchar(255)	NULL,
                         `status`	varchar(20)	NULL,
                         `created_at`	timestamp	DEFAULT CURRENT_TIMESTAMP,
                         `updated_at`	timestamp	DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`)
);

CREATE TABLE `departments` (
                               `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                               `organization_id`	bigint unsigned	NOT NULL,
                               `name`	varchar(100)	NULL,
                               PRIMARY KEY (`id`),
                               CONSTRAINT `fk_organizations_to_departments` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`)
);

CREATE TABLE `positions` (
                             `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                             `organization_id`	bigint unsigned	NOT NULL,
                             `title`	varchar(100)	NULL,
                             PRIMARY KEY (`id`),
                             CONSTRAINT `fk_organizations_to_positions` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`)
);

CREATE TABLE `user_organizations` (
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

CREATE TABLE `role_permissions` (
                                    `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                                    `role_id`	bigint unsigned	NOT NULL,
                                    `permission_id`	int unsigned	NOT NULL,
                                    PRIMARY KEY (`id`),
                                    CONSTRAINT `fk_roles_to_role_permissions` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
                                    CONSTRAINT `fk_permissions_to_role_permissions` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
                                    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
);

CREATE TABLE `user_roles` (
                              `id`	bigint unsigned	NOT NULL AUTO_INCREMENT,
                              `role_id`	bigint unsigned	NOT NULL,
                              `user_organization_id`	bigint unsigned	NOT NULL,
                              PRIMARY KEY (`id`),
                              CONSTRAINT `fk_roles_to_user_roles` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
                              CONSTRAINT `fk_user_organizations_to_user_roles` FOREIGN KEY (`user_organization_id`) REFERENCES `user_organizations` (`id`),
                              UNIQUE KEY `uk_user_role` (`role_id`, `user_organization_id`)
);

-- 성능 최적화를 위한 인덱스
CREATE INDEX `idx_users_email` ON `users` (`email`);
CREATE INDEX `idx_users_status` ON `users` (`status`);
CREATE INDEX `idx_user_organizations_user` ON `user_organizations` (`user_id`);
CREATE INDEX `idx_user_organizations_org` ON `user_organizations` (`organization_id`);
CREATE INDEX `idx_user_organizations_status` ON `user_organizations` (`status`);
CREATE INDEX `idx_roles_org` ON `roles` (`organization_id`);
CREATE INDEX `idx_permissions_resource` ON `permissions` (`resource`);
CREATE INDEX `idx_permissions_active` ON `permissions` (`is_active`);