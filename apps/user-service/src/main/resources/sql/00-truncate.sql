-- 데이터 초기화 전에 추가
SET FOREIGN_KEY_CHECKS = 0;

-- 역순으로 TRUNCATE (참조되는 테이블을 나중에)
TRUNCATE TABLE user_roles;
TRUNCATE TABLE role_permissions;
TRUNCATE TABLE user_organizations;
TRUNCATE TABLE users;
TRUNCATE TABLE positions;
TRUNCATE TABLE departments;
TRUNCATE TABLE roles;
TRUNCATE TABLE permissions;
TRUNCATE TABLE organizations;

SET FOREIGN_KEY_CHECKS = 1;