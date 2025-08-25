-- 데이터 삽입
INSERT INTO "USER" ("user_id", "name", "email", "password", "phone_number", "type", "status", "joined_at")
VALUES
    ('86b2414f-8e4d-4c3e-953e-1b6c7003c271', '홍길동', 'hong.gildong@example.com', 'hashed_password_1', '010-1234-5678', 'INDIVIDUAL', 'ACTIVE', NOW()),
    ('92d04a8b-185d-4f1b-85d1-9650d99d1234', '김철수', 'kim.chulsu@example.com', 'hashed_1b590e829a28', '010-9876-5432', 'INDIVIDUAL', 'ACTIVE', NOW());

INSERT INTO "GROUP_INFO" ("group_id", "name", "description", "status")
VALUES
    ('0b5c1c4e-5e2a-438d-8c1d-1d2a3e3b4d5a', '개발팀', '애플리케이션 개발 그룹', 'ACTIVE'),
    ('5c3f7b2c-8a1e-45a8-9d2a-7e7f6a8e9d2b', '기획팀', '프로젝트 기획 그룹', 'ACTIVE');

INSERT INTO "USER_GROUP" ("user_id", "group_id")
VALUES
    ('86b2414f-8e4d-4c3e-953e-1b6c7003c271', '0b5c1c4e-5e2a-438d-8c1d-1d2a3e3b4d5a'),
    ('92d04a8b-185d-4f1b-85d1-9650d99d1234', '5c3f7b2c-8a1e-45a8-9d2a-7e7f6a8e9d2b');

INSERT INTO "ROLE" ("role_id", "name", "code", "description", "status")
VALUES
    ('e2c3a5f9-8d1a-4b72-9c3f-4e3b2c1d8a1e', '관리자', 'ADMIN', '모든 권한을 가진 역할', 'ACTIVE'),
    ('d1a2c3b4-5f6e-7d8c-9a0b-1c2d3e4f5a6b', '일반 사용자', 'USER', '기본 권한을 가진 역할', 'ACTIVE');

INSERT INTO "PERMISSION" ("permission_id", "name", "code", "resource", "action", "description")
VALUES
    ('c3f5a2b8-7e1d-4c9a-8b1d-2e3f4a5b6c7d', '사용자 정보 읽기', 'USER_READ', 'USER', 'READ', '사용자 정보 조회 권한'),
    ('b5c6a7d8-1e2f-3a4b-5c6d-7e8f9a0b1c2d', '사용자 정보 수정', 'USER_WRITE', 'USER', 'WRITE', '사용자 정보 수정 권한'),
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', '로그인', 'AUTH_LOGIN', 'AUTH', 'LOGIN', '로그인 권한');

INSERT INTO "USER_ROLE" ("user_id", "role_id")
VALUES
    ('86b2414f-8e4d-4c3e-953e-1b6c7003c271', 'e2c3a5f9-8d1a-4b72-9c3f-4e3b2c1d8a1e'),
    ('92d04a8b-185d-4f1b-85d1-9650d99d1234', 'd1a2c3b4-5f6e-7d8c-9a0b-1c2d3e4f5a6b');

INSERT INTO "ROLE_PERMISSION" ("role_id", "permission_id")
VALUES
    ('e2c3a5f9-8d1a-4b72-9c3f-4e3b2c1d8a1e', 'c3f5a2b8-7e1d-4c9a-8b1d-2e3f4a5b6c7d'),
    ('e2c3a5f9-8d1a-4b72-9c3f-4e3b2c1d8a1e', 'b5c6a7d8-1e2f-3a4b-5c6d-7e8f9a0b1c2d'),
    ('e2c3a5f9-8d1a-4b72-9c3f-4e3b2c1d8a1e', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'),
    ('d1a2c3b4-5f6e-7d8c-9a0b-1c2d3e4f5a6b', 'c3f5a2b8-7e1d-4c9a-8b1d-2e3f4a5b6c7d'),
    ('d1a2c3b4-5f6e-7d8c-9a0b-1c2d3e4f5a6b', 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d');