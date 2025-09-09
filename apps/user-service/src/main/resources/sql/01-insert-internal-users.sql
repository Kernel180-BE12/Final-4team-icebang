-- icebang 내부 직원 전체 INSERT

-- 1. icebang 조직
INSERT INTO `organization` (`name`, `domain_name`) VALUES
    ('icebang', 'icebang.site');

-- 2. icebang 부서들
SET @org_id = (SELECT id FROM organization WHERE domain_name = 'icebang.site' LIMIT 1);

INSERT INTO `department` (`organization_id`, `name`) VALUES
                                                         (@org_id, 'AI개발팀'),
                                                         (@org_id, '데이터팀'),
                                                         (@org_id, '콘텐츠팀'),
                                                         (@org_id, '마케팅팀'),
                                                         (@org_id, '운영팀'),
                                                         (@org_id, '기획팀');

-- 3. icebang 직책들
INSERT INTO `position` (`organization_id`, `title`) VALUES
                                                        (@org_id, 'CEO'),
                                                        (@org_id, 'CTO'),
                                                        (@org_id, '팀장'),
                                                        (@org_id, '시니어'),
                                                        (@org_id, '주니어'),
                                                        (@org_id, '인턴');

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
('campaigns.launch', '캠페인 시작'),
('campaigns.pause', '캠페인 일시정지'),

-- 분석/리포트
('analytics.read', '분석 데이터 조회'),
('analytics.export', '분석 데이터 내보내기'),
('reports.create', '보고서 생성'),
('reports.read', '보고서 조회'),
('reports.export', '보고서 내보내기'),

-- 시스템 관리
('system.settings.read', '시스템 설정 조회'),
('system.settings.update', '시스템 설정 수정'),
('system.logs.read', '시스템 로그 조회'),
('system.backup.create', '시스템 백업 생성'),
('system.backup.restore', '시스템 백업 복원');

-- 5. 시스템 공통 역할
INSERT INTO `role` (`organization_id`, `name`, `description`) VALUES
                                                                  (NULL, 'SUPER_ADMIN', '최고 관리자 - 모든 권한'),
                                                                  (NULL, 'SYSTEM_ADMIN', '시스템 관리자 - 시스템 설정 및 관리'),
                                                                  (NULL, 'ORG_ADMIN', '조직 관리자 - 조직 내 모든 권한'),
                                                                  (NULL, 'USER', '일반 사용자 - 기본 사용 권한'),
                                                                  (NULL, 'GUEST', '게스트 - 제한된 조회 권한');

-- 6. icebang 전용 역할
INSERT INTO `role` (`organization_id`, `name`, `description`) VALUES
                                                                  (@org_id, 'AI_ENGINEER', 'AI 엔지니어 - AI 모델 개발 및 최적화'),
                                                                  (@org_id, 'DATA_SCIENTIST', '데이터 사이언티스트 - 데이터 분석 및 인사이트 도출'),
                                                                  (@org_id, 'CRAWLING_ENGINEER', '크롤링 엔지니어 - 웹 크롤링 시스템 개발'),
                                                                  (@org_id, 'CONTENT_CREATOR', '콘텐츠 크리에이터 - 바이럴 콘텐츠 제작'),
                                                                  (@org_id, 'CONTENT_MANAGER', '콘텐츠 매니저 - 콘텐츠 기획 및 관리'),
                                                                  (@org_id, 'WORKFLOW_ADMIN', '워크플로우 관리자 - 자동화 프로세스 관리'),
                                                                  (@org_id, 'MARKETING_ANALYST', '마케팅 분석가 - 마케팅 성과 분석'),
                                                                  (@org_id, 'OPERATIONS_MANAGER', '운영 매니저 - 시스템 운영 및 모니터링');

-- 7. icebang 직원들
INSERT INTO `user` (`name`, `email`, `password`, `status`) VALUES
                                                               ('김아이스', 'ice.kim@icebang.site', '$2a$10$encrypted_password_hash1', 'ACTIVE'),
                                                               ('박방방', 'bang.park@icebang.site', '$2a$10$encrypted_password_hash2', 'ACTIVE'),
                                                               ('이트렌드', 'trend.lee@icebang.site', '$2a$10$encrypted_password_hash3', 'ACTIVE'),
                                                               ('정바이럴', 'viral.jung@icebang.site', '$2a$10$encrypted_password_hash4', 'ACTIVE'),
                                                               ('최콘텐츠', 'content.choi@icebang.site', '$2a$10$encrypted_password_hash5', 'ACTIVE'),
                                                               ('홍크롤러', 'crawler.hong@icebang.site', '$2a$10$encrypted_password_hash6', 'ACTIVE'),
                                                               ('서데이터', 'data.seo@icebang.site', '$2a$10$encrypted_password_hash7', 'ACTIVE'),
                                                               ('윤워크플로', 'workflow.yoon@icebang.site', '$2a$10$encrypted_password_hash8', 'ACTIVE'),
                                                               ('시스템관리자', 'admin@icebang.site', '$2a$10$encrypted_password_hash0', 'ACTIVE');

-- 8. icebang 직원-조직 연결
INSERT INTO `user_organization` (`user_id`, `organization_id`, `position_id`, `department_id`, `employee_number`, `status`) VALUES
                                                                                                                                ((SELECT id FROM user WHERE email = 'ice.kim@icebang.site'), @org_id, (SELECT id FROM position WHERE title = 'CEO' AND organization_id = @org_id), (SELECT id FROM department WHERE name = '기획팀' AND organization_id = @org_id), 'PLN25001', 'ACTIVE'),
                                                                                                                                ((SELECT id FROM user WHERE email = 'bang.park@icebang.site'), @org_id, (SELECT id FROM position WHERE title = 'CTO' AND organization_id = @org_id), (SELECT id FROM department WHERE name = 'AI개발팀' AND organization_id = @org_id), 'AI25001', 'ACTIVE'),
                                                                                                                                ((SELECT id FROM user WHERE email = 'trend.lee@icebang.site'), @org_id, (SELECT id FROM position WHERE title = '팀장' AND organization_id = @org_id), (SELECT id FROM department WHERE name = '데이터팀' AND organization_id = @org_id), 'DAT25001', 'ACTIVE'),
                                                                                                                                ((SELECT id FROM user WHERE email = 'viral.jung@icebang.site'), @org_id, (SELECT id FROM position WHERE title = '팀장' AND organization_id = @org_id), (SELECT id FROM department WHERE name = '콘텐츠팀' AND organization_id = @org_id), 'CON25001', 'ACTIVE'),
                                                                                                                                ((SELECT id FROM user WHERE email = 'content.choi@icebang.site'), @org_id, (SELECT id FROM position WHERE title = '시니어' AND organization_id = @org_id), (SELECT id FROM department WHERE name = '콘텐츠팀' AND organization_id = @org_id), 'CON25002', 'ACTIVE'),
                                                                                                                                ((SELECT id FROM user WHERE email = 'crawler.hong@icebang.site'), @org_id, (SELECT id FROM position WHERE title = '시니어' AND organization_id = @org_id), (SELECT id FROM department WHERE name = 'AI개발팀' AND organization_id = @org_id), 'AI25002', 'ACTIVE'),
                                                                                                                                ((SELECT id FROM user WHERE email = 'data.seo@icebang.site'), @org_id, (SELECT id FROM position WHERE title = '시니어' AND organization_id = @org_id), (SELECT id FROM department WHERE name = '데이터팀' AND organization_id = @org_id), 'DAT25002', 'ACTIVE'),
                                                                                                                                ((SELECT id FROM user WHERE email = 'workflow.yoon@icebang.site'), @org_id, (SELECT id FROM position WHERE title = '팀장' AND organization_id = @org_id), (SELECT id FROM department WHERE name = '운영팀' AND organization_id = @org_id), 'OPS25001', 'ACTIVE'),
                                                                                                                                ((SELECT id FROM user WHERE email = 'admin@icebang.site'), @org_id, (SELECT id FROM position WHERE title = 'CTO' AND organization_id = @org_id), (SELECT id FROM department WHERE name = '운영팀' AND organization_id = @org_id), 'OPS25000', 'ACTIVE');

-- 9. 역할별 권한 할당

-- SUPER_ADMIN 모든 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM role WHERE name = 'SUPER_ADMIN'),
    id
FROM permission;

-- ORG_ADMIN 조직 내 모든 권한 (시스템 권한 제외)
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM role WHERE name = 'ORG_ADMIN'),
    id
FROM permission
WHERE resource NOT LIKE 'system.%';

-- AI_ENGINEER 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM role WHERE name = 'AI_ENGINEER' AND organization_id = @org_id),
    id
FROM permission
WHERE resource LIKE 'ai.%'
   OR resource LIKE 'crawling.%'
   OR resource LIKE 'workflows.%'
   OR resource IN ('content.read', 'trends.read', 'analytics.read');

-- DATA_SCIENTIST 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM role WHERE name = 'DATA_SCIENTIST' AND organization_id = @org_id),
    id
FROM permission
WHERE resource LIKE 'trends.%'
   OR resource LIKE 'analytics.%'
   OR resource LIKE 'reports.%'
   OR resource IN ('content.read', 'campaigns.read', 'crawling.read');

-- CONTENT_MANAGER 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM role WHERE name = 'CONTENT_MANAGER' AND organization_id = @org_id),
    id
FROM permission
WHERE resource LIKE 'content.%'
   OR resource LIKE 'campaigns.%'
   OR resource LIKE 'trends.%'
   OR resource LIKE 'analytics.%'
   OR resource IN ('users.read.department');

-- WORKFLOW_ADMIN 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM role WHERE name = 'WORKFLOW_ADMIN' AND organization_id = @org_id),
    id
FROM permission
WHERE resource LIKE 'workflows.%'
   OR resource LIKE 'ai.%'
   OR resource LIKE 'crawling.%'
   OR resource LIKE 'system.%'
   OR resource IN ('content.read', 'trends.read', 'analytics.read');

-- 10. icebang 직원별 역할 할당

-- 김아이스(CEO) - ORG_ADMIN
INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'ORG_ADMIN'),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'ice.kim@icebang.site';

-- 박방방(CTO) - AI_ENGINEER + WORKFLOW_ADMIN
INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'AI_ENGINEER' AND organization_id = @org_id),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'bang.park@icebang.site';

INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'WORKFLOW_ADMIN' AND organization_id = @org_id),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'bang.park@icebang.site';

-- 정바이럴(콘텐츠팀장) - CONTENT_MANAGER
INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'CONTENT_MANAGER' AND organization_id = @org_id),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'viral.jung@icebang.site';

-- 이트렌드(데이터팀장) - DATA_SCIENTIST
INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'DATA_SCIENTIST' AND organization_id = @org_id),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'trend.lee@icebang.site';

-- 시스템관리자 - SUPER_ADMIN
INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'SUPER_ADMIN'),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'admin@icebang.site';