-- icebang 내부 직원 전체 INSERT (H2 호환 버전)

-- 1. icebang 조직
INSERT INTO `organization` (`name`, `domain_name`) VALUES
    ('icebang', 'icebang.site');

-- 2. icebang 부서들 (직접 ID로 참조)
INSERT INTO `department` (`organization_id`, `name`) VALUES
                                                         (1, 'AI개발팀'),
                                                         (1, '데이터팀'),
                                                         (1, '콘텐츠팀'),
                                                         (1, '마케팅팀'),
                                                         (1, '운영팀'),
                                                         (1, '기획팀');

-- 3. icebang 직책들 (직접 ID로 참조)
INSERT INTO `position` (`organization_id`, `title`) VALUES
                                                        (1, 'CEO'),
                                                        (1, 'CTO'),
                                                        (1, '팀장'),
                                                        (1, '시니어'),
                                                        (1, '주니어'),
                                                        (1, '인턴');

-- 4. 바이럴 콘텐츠 워크플로우 권한들
INSERT INTO `permission` (`resource`, `description`) VALUES
-- 사용자 관리
('users.create', '사용자 생성'),
('users.read', '사용자 조회'),
('users.read.own', '본인 정보 조회'),
('users.read.department', '부서 내 사용자 조회'),
('users.read.organization', '조직 전체 사용자 조회'),
('users.update', '사용자 정보 수정'),
('users.update.own', '본인 정보 수정'),
('users.delete', '사용자 삭제'),
('users.invite', '사용자 초대'),

-- 조직 관리
('organizations.read', '조직 조회'),
('organizations.settings', '조직 설정 관리'),

-- 부서 관리
('departments.read', '부서 조회'),
('departments.manage', '부서 관리'),

-- 역할/권한 관리
('roles.create', '역할 생성'),
('roles.read', '역할 조회'),
('roles.update', '역할 수정'),
('roles.assign', '역할 할당'),
('permissions.read', '권한 조회'),
('permissions.assign', '권한 할당'),

-- 트렌드 키워드 관리
('trends.read', '트렌드 키워드 조회'),
('trends.create', '트렌드 키워드 등록'),
('trends.update', '트렌드 키워드 수정'),
('trends.delete', '트렌드 키워드 삭제'),
('trends.analyze', '트렌드 분석'),

-- 크롤링 관리
('crawling.create', '크롤링 작업 생성'),
('crawling.read', '크롤링 결과 조회'),
('crawling.update', '크롤링 설정 수정'),
('crawling.delete', '크롤링 데이터 삭제'),
('crawling.execute', '크롤링 실행'),
('crawling.schedule', '크롤링 스케줄 관리'),

-- 콘텐츠 생성
('content.create', '콘텐츠 생성'),
('content.read', '콘텐츠 조회'),
('content.read.own', '본인 콘텐츠만 조회'),
('content.read.department', '부서 콘텐츠 조회'),
('content.read.all', '모든 콘텐츠 조회'),
('content.update', '콘텐츠 수정'),
('content.delete', '콘텐츠 삭제'),
('content.publish', '콘텐츠 발행'),
('content.approve', '콘텐츠 승인'),
('content.reject', '콘텐츠 거절'),

-- AI 모델 관리
('ai.models.read', 'AI 모델 조회'),
('ai.models.create', 'AI 모델 생성'),
('ai.models.update', 'AI 모델 수정'),
('ai.models.delete', 'AI 모델 삭제'),
('ai.models.train', 'AI 모델 학습'),
('ai.models.deploy', 'AI 모델 배포'),

-- 워크플로우 관리
('workflows.create', '워크플로우 생성'),
('workflows.read', '워크플로우 조회'),
('workflows.update', '워크플로우 수정'),
('workflows.delete', '워크플로우 삭제'),
('workflows.execute', '워크플로우 실행'),
('workflows.schedule', '워크플로우 스케줄링'),

-- 캠페인 관리
('campaigns.create', '캠페인 생성'),
('campaigns.read', '캠페인 조회'),
('campaigns.update', '캠페인 수정'),
('campaigns.delete', '캠페인 삭제'),
('campaigns.execute', '캠페인 실행'),

-- 시스템 관리
('system.health', '시스템 상태 조회'),
('system.logs', '시스템 로그 조회'),
('system.backup', '시스템 백업'),
('system.config', '시스템 설정 관리');

-- 5. icebang 역할들
INSERT INTO `role` (`organization_id`, `name`, `description`) VALUES
-- 글로벌 관리자 역할
(NULL, 'SUPER_ADMIN', '전체 시스템 관리자 - 모든 권한'),
(NULL, 'ORG_ADMIN', '조직 관리자 - 조직별 모든 권한'),

-- icebang 전용 역할들
(1, 'AI_ENGINEER', 'AI 개발자 - AI 모델 관리 및 워크플로우'),
(1, 'DATA_SCIENTIST', '데이터 과학자 - 트렌드 분석 및 데이터 관리'),
(1, 'CONTENT_MANAGER', '콘텐츠 매니저 - 콘텐츠 생성 및 관리'),
(1, 'MARKETING_SPECIALIST', '마케팅 전문가 - 캠페인 관리'),
(1, 'WORKFLOW_ADMIN', '워크플로우 관리자 - 워크플로우 전체 관리'),
(1, 'CRAWLER_OPERATOR', '크롤링 운영자 - 크롤링 작업 관리'),
(1, 'BASIC_USER', '기본 사용자 - 기본 조회 권한');

-- 6. icebang 직원들
INSERT INTO `user` (`name`, `email`, `password`, `status`) VALUES
('김아이스', 'ice.kim@icebang.site', '$2a$10$encrypted_password_hash1', 'ACTIVE'),
('박방방', 'bang.park@icebang.site', '$2a$10$encrypted_password_hash2', 'ACTIVE'),
('이트렌드', 'trend.lee@icebang.site', '$2a$10$encrypted_password_hash3', 'ACTIVE'),
('정바이럴', 'viral.jung@icebang.site', '$2a$10$encrypted_password_hash4', 'ACTIVE'),
('최콘텐츠', 'content.choi@icebang.site', '$2a$10$encrypted_password_hash5', 'ACTIVE'),
('홍크롤러', 'crawler.hong@icebang.site', '$2a$10$encrypted_password_hash6', 'ACTIVE'),
('서데이터', 'data.seo@icebang.site', '$2a$10$encrypted_password_hash7', 'ACTIVE'),
('윤워크플로우', 'workflow.yoon@icebang.site', '$2a$10$encrypted_password_hash8', 'ACTIVE'),
('시스템관리자', 'admin@icebang.site', '$2a$10$encrypted_password_hash9', 'ACTIVE');

-- 7. icebang 직원들의 조직 소속 정보 (하드코딩된 ID 사용)
INSERT INTO `user_organization` (`user_id`, `organization_id`, `position_id`, `department_id`, `employee_number`, `status`) VALUES
-- 김아이스(CEO) - 기획팀
(1, 1, 1, 6, 'PLN25001', 'ACTIVE'),
-- 박방방(CTO) - AI개발팀
(2, 1, 2, 1, 'AI25001', 'ACTIVE'),
-- 이트렌드(팀장) - 데이터팀
(3, 1, 3, 2, 'DAT25001', 'ACTIVE'),
-- 정바이럴(팀장) - 콘텐츠팀
(4, 1, 3, 3, 'CON25001', 'ACTIVE'),
-- 최콘텐츠(시니어) - 콘텐츠팀
(5, 1, 4, 3, 'CON25002', 'ACTIVE'),
-- 홍크롤러(시니어) - AI개발팀
(6, 1, 4, 1, 'AI25002', 'ACTIVE'),
-- 서데이터(시니어) - 데이터팀
(7, 1, 4, 2, 'DAT25002', 'ACTIVE'),
-- 윤워크플로우(팀장) - 운영팀
(8, 1, 3, 5, 'OPS25001', 'ACTIVE'),
-- 시스템관리자(CTO) - 운영팀
(9, 1, 2, 5, 'OPS25000', 'ACTIVE');

-- 8. 역할별 권한 설정

-- SUPER_ADMIN - 모든 권한 (전역)
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1, id
FROM permission;

-- ORG_ADMIN - 조직 관련 모든 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 2, id
FROM permission
WHERE resource NOT LIKE 'system.%';

-- AI_ENGINEER - AI 및 워크플로우 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 3, id
FROM permission
WHERE resource LIKE 'ai.%'
   OR resource LIKE 'workflows.%'
   OR resource LIKE 'crawling.%'
   OR resource IN ('content.read', 'trends.read');

-- DATA_SCIENTIST - 데이터 및 분석 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 4, id
FROM permission
WHERE resource LIKE 'trends.%'
   OR resource LIKE 'crawling.%'
   OR resource LIKE 'ai.models.read'
   OR resource IN ('content.read', 'workflows.read');

-- CONTENT_MANAGER - 콘텐츠 관리 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 5, id
FROM permission
WHERE resource LIKE 'content.%'
   OR resource LIKE 'campaigns.%'
   OR resource IN ('trends.read', 'workflows.read');

-- MARKETING_SPECIALIST - 마케팅 및 캠페인 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 6, id
FROM permission
WHERE resource LIKE 'campaigns.%'
   OR resource IN ('content.read', 'trends.read', 'users.read');

-- WORKFLOW_ADMIN - 워크플로우 전체 관리 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 7, id
FROM permission
WHERE resource LIKE 'workflows.%'
   OR resource LIKE 'ai.%'
   OR resource LIKE 'crawling.%'
   OR resource LIKE 'system.%'
   OR resource IN ('content.read', 'trends.read');

-- 9. icebang 직원별 역할 할당

-- 김아이스(CEO) - ORG_ADMIN
INSERT INTO `user_role` (`role_id`, `user_organization_id`) VALUES (2, 1);

-- 박방방(CTO) - AI_ENGINEER + WORKFLOW_ADMIN
INSERT INTO `user_role` (`role_id`, `user_organization_id`) VALUES (3, 2), (7, 2);

-- 정바이럴(콘텐츠팀장) - CONTENT_MANAGER
INSERT INTO `user_role` (`role_id`, `user_organization_id`) VALUES (5, 4);

-- 이트렌드(데이터팀장) - DATA_SCIENTIST
INSERT INTO `user_role` (`role_id`, `user_organization_id`) VALUES (4, 3);

-- 시스템관리자 - SUPER_ADMIN
INSERT INTO `user_role` (`role_id`, `user_organization_id`) VALUES (1, 9);