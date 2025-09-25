-- 데이터 초기화 스크립트 (외래 키 제약조건이 없는 스키마용)

-- 사용자 및 조직 관련 테이블
TRUNCATE TABLE `user_role`;
TRUNCATE TABLE `role_permission`;
TRUNCATE TABLE `user_organization`;
TRUNCATE TABLE `user`;
TRUNCATE TABLE `position`;
TRUNCATE TABLE `department`;
TRUNCATE TABLE `role`;
TRUNCATE TABLE `permission`;
TRUNCATE TABLE `organization`;