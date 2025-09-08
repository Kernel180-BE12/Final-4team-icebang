-- B2B 테스트용 외부 회사 INSERT

-- 1. 외부 테스트 회사들
INSERT INTO `organizations` (`name`, `domain_name`) VALUES
                                                        ('테크이노베이션', 'techinnovation.co.kr'),
                                                        ('디지털솔루션', 'digitalsolution.com'),
                                                        ('크리에이티브웍스', 'creativeworks.net');

-- 2. 테크이노베이션 부서들
INSERT INTO `departments` (`organization_id`, `name`) VALUES
                                                          ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '개발팀'),
                                                          ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '디자인팀'),
                                                          ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '인사팀'),
                                                          ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '마케팅팀'),
                                                          ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '영업팀'),
                                                          ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '재무팀');

-- 3. 디지털솔루션 부서들
INSERT INTO `departments` (`organization_id`, `name`) VALUES
                                                          ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), '개발팀'),
                                                          ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), '기획팀'),
                                                          ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), '운영팀');

-- 4. 크리에이티브웍스 부서들
INSERT INTO `departments` (`organization_id`, `name`) VALUES
                                                          ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), '디자인팀'),
                                                          ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), '마케팅팀'),
                                                          ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), '제작팀');

-- 5. 테크이노베이션 직책들
INSERT INTO `positions` (`organization_id`, `title`) VALUES
                                                         ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '사원'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '주임'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '대리'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '과장'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '차장'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '부장'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), '이사');

-- 6. 디지털솔루션 직책들
INSERT INTO `positions` (`organization_id`, `title`) VALUES
                                                         ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), '사원'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), '선임'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), '책임'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), '수석'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), '팀장'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), '본부장');

-- 7. 크리에이티브웍스 직책들
INSERT INTO `positions` (`organization_id`, `title`) VALUES
                                                         ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), '주니어'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), '시니어'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), '리드'),
                                                         ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), '디렉터');

-- 8. 외부 회사별 커스텀 역할

-- 테크이노베이션 역할
INSERT INTO `roles` (`organization_id`, `name`, `description`) VALUES
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), 'DEPT_MANAGER', '부서 관리자 - 부서 내 관리 권한'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), 'TEAM_LEAD', '팀장 - 팀원 관리 및 프로젝트 리드'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), 'SENIOR_DEV', '시니어 개발자 - 개발 관련 고급 권한'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), 'JUNIOR_DEV', '주니어 개발자 - 개발 관련 기본 권한'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), 'PROJECT_MANAGER', '프로젝트 매니저 - 프로젝트 관리 권한'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), 'DESIGNER', '디자이너 - 디자인 관련 권한'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'), 'HR_SPECIALIST', '인사 담당자 - 인사 관리 권한');

-- 디지털솔루션 역할
INSERT INTO `roles` (`organization_id`, `name`, `description`) VALUES
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), 'TECH_LEAD', '기술 리드 - 기술 관련 총괄'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), 'PRODUCT_OWNER', '프로덕트 오너 - 제품 기획 관리'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), 'QA_ENGINEER', 'QA 엔지니어 - 품질 보증'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'), 'DEVOPS', 'DevOps 엔지니어 - 인프라 관리');

-- 크리에이티브웍스 역할
INSERT INTO `roles` (`organization_id`, `name`, `description`) VALUES
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), 'CREATIVE_DIRECTOR', '크리에이티브 디렉터 - 창작 총괄'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), 'ART_DIRECTOR', '아트 디렉터 - 예술 감독'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), 'MOTION_DESIGNER', '모션 디자이너 - 영상/애니메이션'),
                                                                   ((SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'), 'COPYWRITER', '카피라이터 - 콘텐츠 작성');

-- 9. 외부 회사 테스트 사용자들
INSERT INTO `users` (`name`, `email`, `password`, `status`) VALUES
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
INSERT INTO `user_organizations` (`user_id`, `organization_id`, `position_id`, `department_id`, `employee_number`, `status`) VALUES
-- 테크이노베이션 직원들
-- 김철수 - 개발팀 과장
((SELECT id FROM users WHERE email = 'chulsoo.kim@techinnovation.co.kr'),
 (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'),
 (SELECT id FROM positions WHERE title = '과장' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr')),
 (SELECT id FROM departments WHERE name = '개발팀' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr')),
 'DEV25001', 'ACTIVE'),

-- 이영희 - 디자인팀 대리
((SELECT id FROM users WHERE email = 'younghee.lee@techinnovation.co.kr'),
 (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'),
 (SELECT id FROM positions WHERE title = '대리' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr')),
 (SELECT id FROM departments WHERE name = '디자인팀' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr')),
 'DES25001', 'ACTIVE'),

-- 박민수 - 인사팀 차장
((SELECT id FROM users WHERE email = 'minsu.park@techinnovation.co.kr'),
 (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr'),
 (SELECT id FROM positions WHERE title = '차장' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr')),
 (SELECT id FROM departments WHERE name = '인사팀' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr')),
 'HR25001', 'ACTIVE'),

-- 디지털솔루션 직원들
-- 정수연 - 개발팀 팀장
((SELECT id FROM users WHERE email = 'sooyeon.jung@digitalsolution.com'),
 (SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'),
 (SELECT id FROM positions WHERE title = '팀장' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com')),
 (SELECT id FROM departments WHERE name = '개발팀' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com')),
 'DEV25001', 'ACTIVE'),

-- 최현우 - 기획팀 책임
((SELECT id FROM users WHERE email = 'hyunwoo.choi@digitalsolution.com'),
 (SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com'),
 (SELECT id FROM positions WHERE title = '책임' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com')),
 (SELECT id FROM departments WHERE name = '기획팀' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com')),
 'PLN25001', 'ACTIVE'),

-- 크리에이티브웍스 직원
-- 홍지아 - 디자인팀 리드
((SELECT id FROM users WHERE email = 'jia.hong@creativeworks.net'),
 (SELECT id FROM organizations WHERE domain_name = 'creativeworks.net'),
 (SELECT id FROM positions WHERE title = '리드' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'creativeworks.net')),
 (SELECT id FROM departments WHERE name = '디자인팀' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'creativeworks.net')),
 'DES25001', 'ACTIVE');

-- 11. 외부 회사 사용자별 역할 할당

-- 테크이노베이션
-- 김철수에게 DEPT_MANAGER 역할
INSERT INTO `user_roles` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM roles WHERE name = 'DEPT_MANAGER' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr')),
    uo.id
FROM user_organizations uo
         JOIN users u ON u.id = uo.user_id
WHERE u.email = 'chulsoo.kim@techinnovation.co.kr';

-- 이영희에게 DESIGNER 역할
INSERT INTO `user_roles` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM roles WHERE name = 'DESIGNER' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr')),
    uo.id
FROM user_organizations uo
         JOIN users u ON u.id = uo.user_id
WHERE u.email = 'younghee.lee@techinnovation.co.kr';

-- 박민수에게 HR_SPECIALIST 역할
INSERT INTO `user_roles` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM roles WHERE name = 'HR_SPECIALIST' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr')),
    uo.id
FROM user_organizations uo
         JOIN users u ON u.id = uo.user_id
WHERE u.email = 'minsu.park@techinnovation.co.kr';

-- 디지털솔루션
-- 정수연에게 TECH_LEAD 역할
INSERT INTO `user_roles` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM roles WHERE name = 'TECH_LEAD' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com')),
    uo.id
FROM user_organizations uo
         JOIN users u ON u.id = uo.user_id
WHERE u.email = 'sooyeon.jung@digitalsolution.com';

-- 최현우에게 PRODUCT_OWNER 역할
INSERT INTO `user_roles` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM roles WHERE name = 'PRODUCT_OWNER' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com')),
    uo.id
FROM user_organizations uo
         JOIN users u ON u.id = uo.user_id
WHERE u.email = 'hyunwoo.choi@digitalsolution.com';

-- 크리에이티브웍스
-- 홍지아에게 CREATIVE_DIRECTOR 역할
INSERT INTO `user_roles` (`role_id`, `user_organization_id`)
SELECT
    (SELECT id FROM roles WHERE name = 'CREATIVE_DIRECTOR' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'creativeworks.net')),
    uo.id
FROM user_organizations uo
         JOIN users u ON u.id = uo.user_id
WHERE u.email = 'jia.hong@creativeworks.net';

-- 12. 외부 회사 역할별 기본 권한 할당 (샘플)

-- DEPT_MANAGER 권한
INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM roles WHERE name = 'DEPT_MANAGER' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'techinnovation.co.kr')),
    id
FROM permissions
WHERE resource IN (
                   'users.read.department', 'users.update', 'users.invite',
                   'departments.read', 'departments.manage',
                   'content.create', 'content.read.all', 'content.update', 'content.approve',
                   'campaigns.create', 'campaigns.read', 'campaigns.update',
                   'analytics.read', 'reports.read'
    );

-- TECH_LEAD 권한
INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM roles WHERE name = 'TECH_LEAD' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'digitalsolution.com')),
    id
FROM permissions
WHERE resource LIKE 'ai.%'
   OR resource LIKE 'workflows.%'
   OR resource IN (
                   'users.read.department',
                   'content.read', 'content.create',
                   'trends.read', 'analytics.read'
    );

-- CREATIVE_DIRECTOR 권한
INSERT INTO `role_permissions` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM roles WHERE name = 'CREATIVE_DIRECTOR' AND organization_id = (SELECT id FROM organizations WHERE domain_name = 'creativeworks.net')),
    id
FROM permissions
WHERE resource LIKE 'content.%'
   OR resource LIKE 'campaigns.%'
   OR resource IN (
                   'users.read.organization',
                   'trends.read', 'analytics.read', 'reports.create'
    );