INSERT INTO "USER" ("user_id", "name", "email", "password", "phone_number", "type", "status", "joined_at")
VALUES
    ('86b2414f-8e4d-4c3e-953e-1b6c7003c271', '홍길동', 'hong.gildong@example.com', 'hashed_password_1', '010-1234-5678', 'INDIVIDUAL', 'ACTIVE', NOW()),
    ('92d04a8b-185d-4f1b-85d1-9650d99d1234', '김철수', 'kim.chulsu@example.com', 'hashed_1b590e829a28', '010-9876-5432', 'INDIVIDUAL', 'ACTIVE', NOW());

INSERT INTO "GROUP_INFO" ("name", "description", "status")
VALUES
    ('개발팀', '애플리케이션 개발 그룹', 'ACTIVE'), -- ID 1로 생성됨
    ('기획팀', '프로젝트 기획 그룹', 'ACTIVE');      -- ID 2로 생성됨

INSERT INTO "USER_GROUP_INFO" ("user_id", "group_info_id")
VALUES
    ('86b2414f-8e4d-4c3e-953e-1b6c7003c271', 1), -- 홍길동 -> 개발팀
    ('92d04a8b-185d-4f1b-85d1-9650d99d1234', 2); -- 김철수 -> 기획팀

INSERT INTO "ROLE" ("name", "code", "description", "status")
VALUES
    ('관리자', 'ADMIN', '모든 권한을 가진 역할', 'ACTIVE'),     -- ID 1로 생성됨
    ('일반 사용자', 'USER', '기본 권한을 가진 역할', 'ACTIVE'); -- ID 2로 생성됨

INSERT INTO "PERMISSION" ("name", "code", "resource", "action", "description")
VALUES
    ('사용자 정보 읽기', 'USER_READ', 'USER', 'READ', '사용자 정보 조회 권한'), -- ID 1로 생성됨
    ('사용자 정보 수정', 'USER_WRITE', 'USER', 'WRITE', '사용자 정보 수정 권한'), -- ID 2로 생성됨
    ('로그인', 'AUTH_LOGIN', 'AUTH', 'LOGIN', '로그인 권한'); -- ID 3으로 생성됨

INSERT INTO "USER_ROLE" ("user_id", "role_id")
VALUES
    ('86b2414f-8e4d-4c3e-953e-1b6c7003c271', 1), -- 홍길동 -> 관리자
    ('92d04a8b-185d-4f1b-85d1-9650d99d1234', 2); -- 김철수 -> 일반 사용자

INSERT INTO "ROLE_PERMISSION" ("role_id", "permission_id")
VALUES
    (1, 1), -- 관리자 -> 사용자 정보 읽기
    (1, 2), -- 관리자 -> 사용자 정보 수정
    (1, 3), -- 관리자 -> 로그인
    (2, 1), -- 일반 사용자 -> 사용자 정보 읽기
    (2, 3); -- 일반 사용자 -> 로그인