SET FOREIGN_KEY_CHECKS = 0;
SET @tables = NULL;

-- 1. 데이터베이스 내 모든 테이블 목록을 가져와 변수에 저장
-- 백틱(`)을 사용하여 테이블 이름에 공백이나 특수 문자가 있어도 안전하게 처리합니다.
SELECT GROUP_CONCAT(CONCAT('`', table_name, '`')) INTO @tables
FROM information_schema.tables
WHERE table_schema = DATABASE();

-- 2. 변수 값이 NULL인 경우를 대비하여 조건문 추가 및 DROP TABLE 구문 생성
SET @drop_tables_sql = IFNULL(CONCAT('DROP TABLE ', @tables), 'SELECT "No tables to drop";');

-- 3. 동적 SQL 실행
PREPARE stmt FROM @drop_tables_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;