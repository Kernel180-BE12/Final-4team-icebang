-- =======================================================
-- 1단계: 사용자 관리 시스템을 위한 기본 테이블 생성
-- 파일 위치: test/resources/sql/create-user.sql
-- =======================================================

-- 1. 조직 테이블 - 회사나 큰 조직 단위를 관리
CREATE TABLE `ORGANIZATION` (
                                `org_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '조직 ID (기본키)',
                                `org_name` VARCHAR(150) NULL COMMENT '조직명 (예: GLT Korea)',
                                PRIMARY KEY (`org_id`)
) COMMENT='조직 정보 테이블';

-- 2. 부서 테이블 - 조직 내의 부서들을 관리
CREATE TABLE `DEPARTMENT` (
                              `dept_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '부서 ID (기본키)',
                              `org_id` BIGINT NOT NULL COMMENT '소속 조직 ID (외래키)',
                              `dept_name` VARCHAR(100) NULL COMMENT '부서명 (예: Data Engineering Team)',
                              PRIMARY KEY (`dept_id`),
                              FOREIGN KEY (`org_id`) REFERENCES `ORGANIZATION`(`org_id`) COMMENT '조직 테이블과 연결'
) COMMENT='부서 정보 테이블';

-- 3. 직급 테이블 - 부서 내의 직급/포지션을 관리
CREATE TABLE `POSITION` (
                            `position_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '직급 ID (기본키)',
                            `dept_id` BIGINT UNSIGNED NOT NULL COMMENT '소속 부서 ID',
                            `position_title` VARCHAR(100) NULL COMMENT '직급명 (예: Senior Data Engineer)',
                            PRIMARY KEY (`position_id`)
) COMMENT='직급 정보 테이블';

-- 4. 역할 테이블 - 시스템 내 권한 역할을 정의 (SUPER_ADMIN, ADMIN 등)
CREATE TABLE `ROLE` (
                        `role_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '역할 ID (기본키)',
                        `role_name` VARCHAR(100) NULL COMMENT '역할명 (예: SUPER_ADMIN, DATA_ENGINEER)',
                        `role_description` TEXT NULL COMMENT '역할 설명',
                        PRIMARY KEY (`role_id`)
) COMMENT='시스템 권한 역할 테이블';

-- 5. 권한 테이블 - 구체적인 권한들을 정의 (사용자관리, 데이터조회 등)
CREATE TABLE `PERMISSION` (
                              `permission_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '권한 ID (기본키)',
                              `resource` VARCHAR(255) NULL COMMENT '권한 대상 자원 (예: USER, DATA, CONFIG)',
                              `permission_description` VARCHAR(255) NULL COMMENT '권한 설명',
                              `permission_code` INT NULL COMMENT '권한 레벨 코드 (숫자가 높을수록 높은 권한)',
                              `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
                              `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
                              `updated_by` BIGINT NULL COMMENT '수정한 사용자 ID',
                              `created_by` BIGINT NULL COMMENT '생성한 사용자 ID',
                              PRIMARY KEY (`permission_id`)
) COMMENT='시스템 권한 세부사항 테이블';

-- 6. 사용자 테이블 - 실제 로그인하는 사용자 정보
CREATE TABLE `USERS` (
                         `user_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '사용자 ID (기본키)',
                         `user_name` VARCHAR(20) NULL COMMENT '사용자명 (화면에 표시되는 이름)',
                         `user_email` VARCHAR(50) NULL COMMENT '이메일 (로그인 ID로 사용)',
                         `user_password` VARCHAR(255) NULL COMMENT '암호화된 비밀번호',
                         `user_status` VARCHAR(20) NULL COMMENT '계정상태 (ACTIVE: 활성, SUSPENDED: 정지, INACTIVE: 비활성)',
                         `dept_id` BIGINT NOT NULL COMMENT '소속 부서 ID',
                         `position_id` BIGINT NOT NULL COMMENT '직급 ID',
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `uk_user_email` (`user_email`) COMMENT '이메일 중복 방지'
) COMMENT='사용자 기본 정보 테이블';

-- 7. 사용자-역할 매핑 테이블 - 한 사용자가 여러 역할을 가질 수 있도록 관리
CREATE TABLE `USERS_ROLE` (
                              `user_role_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자-역할 매핑 ID (기본키)',
                              `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '역할 부여 시간',
                              `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '역할 수정 시간',
                              `role_id` BIGINT NOT NULL COMMENT '부여된 역할 ID',
                              `user_id` BIGINT UNSIGNED NOT NULL COMMENT '역할을 받은 사용자 ID',
                              PRIMARY KEY (`user_role_id`),
                              FOREIGN KEY (`role_id`) REFERENCES `ROLE`(`role_id`) COMMENT '역할 테이블과 연결',
                              FOREIGN KEY (`user_id`) REFERENCES `USERS`(`user_id`) COMMENT '사용자 테이블과 연결',
                              UNIQUE KEY `uk_user_role` (`user_id`, `role_id`) COMMENT '동일 사용자-역할 중복 방지'
) COMMENT='사용자와 역할의 다대다 관계 테이블';

-- 8. 역할-권한 매핑 테이블 - 각 역할이 어떤 권한들을 가지는지 관리
CREATE TABLE `ROLE_PERMISSION` (
                                   `role_permission_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '역할-권한 매핑 ID (기본키)',
                                   `role_id` BIGINT NOT NULL COMMENT '권한을 가진 역할 ID',
                                   `permission_id` BIGINT NOT NULL COMMENT '부여된 권한 ID',
                                   PRIMARY KEY (`role_permission_id`),
                                   FOREIGN KEY (`role_id`) REFERENCES `ROLE`(`role_id`) COMMENT '역할 테이블과 연결',
                                   FOREIGN KEY (`permission_id`) REFERENCES `PERMISSION`(`permission_id`) COMMENT '권한 테이블과 연결',
                                   UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`) COMMENT '동일 역할-권한 중복 방지'
) COMMENT='역할과 권한의 다대다 관계 테이블';

-- 9. 사용자-조직 매핑 테이블 - 사용자가 속한 조직 관리 (향후 확장용)
CREATE TABLE `USERS_ORGANIZATION` (
                                      `user_organization_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자-조직 매핑 ID (기본키)',
                                      `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
                                      `org_id` BIGINT NOT NULL COMMENT '조직 ID',
                                      PRIMARY KEY (`user_organization_id`),
                                      FOREIGN KEY (`org_id`) REFERENCES `ORGANIZATION`(`org_id`) COMMENT '조직 테이블과 연결'
) COMMENT='사용자와 조직의 관계 테이블 (멀티 조직 지원용)';

-- =======================================================
-- 테이블 생성 완료
-- 다음 단계: 기본 데이터 입력 (조직, 부서, 직급, 역할, 권한)
-- =======================================================