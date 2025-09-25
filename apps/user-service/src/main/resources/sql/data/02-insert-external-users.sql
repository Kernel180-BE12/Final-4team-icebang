-- B2B 테스트용 외부 회사 INSERT

-- 1. 외부 테스트 회사들
INSERT INTO `organization` (`name`, `domain_name`) VALUES
                                                       ('테크이노베이션', 'techinnovation.co.kr'),
                                                       ('디지털솔루션', 'digitalsolution.com'),
                                                       ('크리에이티브웍스', 'creativeworks.net');

-- 2. 테크이노베이션 부서들
SET @tech_org_id = (SELECT id FROM organization WHERE domain_name = 'techinnovation.co.kr' LIMIT 1);

INSERT INTO `department` (`organization_id`, `name`) VALUES
                                                         (@tech_org_id, '개발팀'),
                                                         (@tech_org_id, '디자인팀'),
                                                         (@tech_org_id, '인사팀'),
                                                         (@tech_org_id, '마케팅팀'),
                                                         (@tech_org_id, '영업팀'),
                                                         (@tech_org_id, '재무팀');

-- 3. 디지털솔루션 부서들
SET @digital_org_id = (SELECT id FROM organization WHERE domain_name = 'digitalsolution.com' LIMIT 1);

INSERT INTO `department` (`organization_id`, `name`) VALUES
                                                         (@digital_org_id, '개발팀'),
                                                         (@digital_org_id, '기획팀'),
                                                         (@digital_org_id, '운영팀');

-- 4. 크리에이티브웍스 부서들
SET @creative_org_id = (SELECT id FROM organization WHERE domain_name = 'creativeworks.net' LIMIT 1);

INSERT INTO `department` (`organization_id`, `name`) VALUES
                                                         (@creative_org_id, '디자인팀'),
                                                         (@creative_org_id, '마케팅팀'),
                                                         (@creative_org_id, '제작팀');

-- 5. 테크이노베이션 직책들
INSERT INTO `position` (`organization_id`, `title`) VALUES
                                                        (@tech_org_id, '사원'),
                                                        (@tech_org_id, '주임'),
                                                        (@tech_org_id, '대리'),
                                                        (@tech_org_id, '과장'),
                                                        (@tech_org_id, '차장'),
                                                        (@tech_org_id, '부장'),
                                                        (@tech_org_id, '이사');

-- 6. 디지털솔루션 직책들
INSERT INTO `position` (`organization_id`, `title`) VALUES
                                                        (@digital_org_id, '사원'),
                                                        (@digital_org_id, '선임'),
                                                        (@digital_org_id, '책임'),
                                                        (@digital_org_id, '수석'),
                                                        (@digital_org_id, '팀장'),
                                                        (@digital_org_id, '본부장');

-- 7. 크리에이티브웍스 직책들
INSERT INTO `position` (`organization_id`, `title`) VALUES
                                                        (@creative_org_id, '주니어'),
                                                        (@creative_org_id, '시니어'),
                                                        (@creative_org_id, '리드'),
                                                        (@creative_org_id, '디렉터');

-- 8. 외부 회사별 커스텀 역할

-- 테크이노베이션 역할
INSERT INTO `role` (`organization_id`, `name`, `description`) VALUES
                                                                  (@tech_org_id, 'DEPT_MANAGER', '부서 관리자 - 부서 내 관리 권한'),
                                                                  (@tech_org_id, 'TEAM_LEAD', '팀장 - 팀원 관리 및 프로젝트 리드'),
                                                                  (@tech_org_id, 'SENIOR_DEV', '시니어 개발자 - 개발 관련 고급 권한'),
                                                                  (@tech_org_id, 'JUNIOR_DEV', '주니어 개발자 - 개발 관련 기본 권한'),
                                                                  (@tech_org_id, 'PROJECT_MANAGER', '프로젝트 매니저 - 프로젝트 관리 권한'),
                                                                  (@tech_org_id, 'DESIGNER', '디자이너 - 디자인 관련 권한'),
                                                                  (@tech_org_id, 'HR_SPECIALIST', '인사 담당자 - 인사 관리 권한');

-- 디지털솔루션 역할
INSERT INTO `role` (`organization_id`, `name`, `description`) VALUES
                                                                  (@digital_org_id, 'TECH_LEAD', '기술 리드 - 기술 관련 총괄'),
                                                                  (@digital_org_id, 'PRODUCT_OWNER', '프로덕트 오너 - 제품 기획 관리'),
                                                                  (@digital_org_id, 'QA_ENGINEER', 'QA 엔지니어 - 품질 보증'),
                                                                  (@digital_org_id, 'DEVOPS', 'DevOps 엔지니어 - 인프라 관리');

-- 크리에이티브웍스 역할
INSERT INTO `role` (`organization_id`, `name`, `description`) VALUES
                                                                  (@creative_org_id, 'CREATIVE_DIRECTOR', '크리에이티브 디렉터 - 창작 총괄'),
                                                                  (@creative_org_id, 'ART_DIRECTOR', '아트 디렉터 - 예술 감독'),
                                                                  (@creative_org_id, 'MOTION_DESIGNER', '모션 디자이너 - 영상/애니메이션'),
                                                                  (@creative_org_id, 'COPYWRITER', '카피라이터 - 콘텐츠 작성');

-- 9. 외부 회사 테스트 사용자들
INSERT INTO `user` (`name`, `email`, `password`, `status`) VALUES
-- 테크이노베이션 직원
('김철수', 'chulsoo.kim@techinnovation.co.kr', '$2a$10$encrypted_password_hash11', 'ACTIVE'),
('이영희', 'younghee.lee@techinnovation.co.kr', '$2a$10$encrypted_password_hash12', 'ACTIVE'),
('박민수', 'minsu.park@techinnovation.co.kr', '$2a$10$encrypted_password_hash13', 'ACTIVE'),

-- 디지털솔루션 직원
('정수연', 'sooyeon.jung@digitalsolution.com', '$2a$10$encrypted_password_hash14', 'ACTIVE'),
('최현우', 'hyunwoo.choi@digitalsolution.com', '$2a$10$encrypted_password_hash15', 'ACTIVE'),

-- 크리에이티브웍스 직원
('홍지아', 'jia.hong@creativeworks.net', '$2a$10$encrypted_password_hash16', 'ACTIVE');

-- 10. 외부 회사 사용자-조직 연결
INSERT INTO `user_organization` (`user_id`, `organization_id`, `position_id`, `department_id`, `employee_number`, `status`) VALUES
-- 테크이노베이션 직원들
((SELECT id FROM user WHERE email = 'chulsoo.kim@techinnovation.co.kr'), @tech_org_id, (SELECT id FROM position WHERE title = '과장' AND organization_id = @tech_org_id), (SELECT id FROM department WHERE name = '개발팀' AND organization_id = @tech_org_id), 'DEV25001', 'ACTIVE'),
((SELECT id FROM user WHERE email = 'younghee.lee@techinnovation.co.kr'), @tech_org_id, (SELECT id FROM position WHERE title = '대리' AND organization_id = @tech_org_id), (SELECT id FROM department WHERE name = '디자인팀' AND organization_id = @tech_org_id), 'DES25001', 'ACTIVE'),
((SELECT id FROM user WHERE email = 'minsu.park@techinnovation.co.kr'), @tech_org_id, (SELECT id FROM position WHERE title = '차장' AND organization_id = @tech_org_id), (SELECT id FROM department WHERE name = '인사팀' AND organization_id = @tech_org_id), 'HR25001', 'ACTIVE'),

-- 디지털솔루션 직원들
((SELECT id FROM user WHERE email = 'sooyeon.jung@digitalsolution.com'), @digital_org_id, (SELECT id FROM position WHERE title = '팀장' AND organization_id = @digital_org_id), (SELECT id FROM department WHERE name = '개발팀' AND organization_id = @digital_org_id), 'DEV25001', 'ACTIVE'),
((SELECT id FROM user WHERE email = 'hyunwoo.choi@digitalsolution.com'), @digital_org_id, (SELECT id FROM position WHERE title = '책임' AND organization_id = @digital_org_id), (SELECT id FROM department WHERE name = '기획팀' AND organization_id = @digital_org_id), 'PLN25001', 'ACTIVE'),

-- 크리에이티브웍스 직원
((SELECT id FROM user WHERE email = 'jia.hong@creativeworks.net'), @creative_org_id, (SELECT id FROM position WHERE title = '리드' AND organization_id = @creative_org_id), (SELECT id FROM department WHERE name = '디자인팀' AND organization_id = @creative_org_id), 'DES25001', 'ACTIVE');

-- 11. 외부 회사 사용자별 역할 할당

-- 테크이노베이션
INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'DEPT_MANAGER' AND organization_id = @tech_org_id),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'chulsoo.kim@techinnovation.co.kr';

INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'DESIGNER' AND organization_id = @tech_org_id),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'younghee.lee@techinnovation.co.kr';

INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'HR_SPECIALIST' AND organization_id = @tech_org_id),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'minsu.park@techinnovation.co.kr';

-- 디지털솔루션
INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'TECH_LEAD' AND organization_id = @digital_org_id),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'sooyeon.jung@digitalsolution.com';

INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'PRODUCT_OWNER' AND organization_id = @digital_org_id),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'hyunwoo.choi@digitalsolution.com';

-- 크리에이티브웍스
INSERT INTO `user_role` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM role WHERE name = 'CREATIVE_DIRECTOR' AND organization_id = @creative_org_id),
    uo.id
FROM user_organization uo
         JOIN user u ON u.id = uo.user_id
WHERE u.email = 'jia.hong@creativeworks.net';

-- 12. 외부 회사 역할별 기본 권한 할당 (샘플)

-- DEPT_MANAGER 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM role WHERE name = 'DEPT_MANAGER' AND organization_id = @tech_org_id),
    id
FROM permission
WHERE resource IN (
                   'users.read.department', 'users.update', 'users.invite',
                   'departments.read', 'departments.manage',
                   'content.create', 'content.read.all', 'content.update', 'content.approve',
                   'campaigns.create', 'campaigns.read', 'campaigns.update',
                   'analytics.read', 'reports.read'
    );

-- TECH_LEAD 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM role WHERE name = 'TECH_LEAD' AND organization_id = @digital_org_id),
    id
FROM permission
WHERE resource LIKE 'ai.%'
   OR resource LIKE 'workflows.%'
   OR resource IN (
                   'users.read.department',
                   'content.read', 'content.create',
                   'trends.read', 'analytics.read'
    );

-- CREATIVE_DIRECTOR 권한
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM role WHERE name = 'CREATIVE_DIRECTOR' AND organization_id = @creative_org_id),
    id
FROM permission
WHERE resource LIKE 'content.%'
   OR resource LIKE 'campaigns.%'
   OR resource IN (
                   'users.read.organization',
                   'trends.read', 'analytics.read', 'reports.create'
    );