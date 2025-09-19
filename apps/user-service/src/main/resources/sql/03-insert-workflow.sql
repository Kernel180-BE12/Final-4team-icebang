-- 기존 워크플로우 관련 데이터 삭제
DELETE FROM `job_task`;
DELETE FROM `workflow_job`;
DELETE FROM `task`;
DELETE FROM `job`;
DELETE FROM `workflow`;
DELETE FROM `schedule`;

-- 워크플로우 생성 (ID: 1)
INSERT INTO `workflow` (`id`, `name`, `description`) VALUES
    (1, '상품 분석 및 블로그 자동 발행', '키워드 검색부터 상품 분석 후 블로그 발행까지의 자동화 프로세스');

-- Job 생성 (ID: 1, 2)
INSERT INTO `job` (`id`, `name`, `description`) VALUES
                                                    (1, '상품 분석', '키워드 검색, 상품 크롤링 및 유사도 분석 작업'),
                                                    (2, '블로그 콘텐츠 생성', '분석 데이터를 기반으로 RAG 콘텐츠 생성 및 발행 작업');



-- Task 생성 (ID: 1 ~ 7) - parameters.body의 value를 'Java 타입'으로 변경
INSERT INTO `task` (`id`, `name`, `type`, `parameters`) VALUES
                                                            -- Job 1의 Task들
                                                            (1, '키워드 검색 태스크', 'HTTP', JSON_OBJECT(
                                                                    'endpoint', '/keywords/search', 'method', 'POST',
                                                                    'body', JSON_OBJECT('tag', 'String')
                                                                                      )),
                                                            (2, '상품 검색 태스크', 'HTTP', JSON_OBJECT(
                                                                    'endpoint', '/products/search', 'method', 'POST',
                                                                    'body', JSON_OBJECT('keyword', 'String')
                                                                                     )),
                                                            (3, '상품 매칭 태스크', 'HTTP', JSON_OBJECT(
                                                                    'endpoint', '/products/match', 'method', 'POST',
                                                                    'body', JSON_OBJECT(
                                                                            'keyword', 'String',
                                                                            'search_results', 'List' -- 또는 'JsonNode' 등 약속된 타입
                                                                            )
                                                                                     )),
                                                            -- Body가 필요 없는 Task들은 비워둠
                                                            (4, '상품 유사도 분석 태스크', 'HTTP', JSON_OBJECT('endpoint', '/products/similarity', 'method', 'POST')),
                                                            (5, '상품 정보 크롤링 태스크', 'HTTP', JSON_OBJECT('endpoint', '/products/crawl', 'method', 'POST')),
                                                            (6, '블로그 RAG 생성 태스크', 'HTTP', JSON_OBJECT('endpoint', '/blogs/rag/create', 'method', 'POST')),
                                                            (7, '블로그 발행 태스크', 'HTTP', JSON_OBJECT('endpoint', '/blogs/publish', 'method', 'POST'))
    ON DUPLICATE KEY UPDATE name = VALUES(name), type = VALUES(type), parameters = VALUES(parameters), updated_at = NOW();



-- 워크플로우-Job 연결
INSERT INTO `workflow_job` (`workflow_id`, `job_id`, `execution_order`) VALUES
                                                                            (1, 1, 1),
                                                                            (1, 2, 2);

-- Job-Task 연결
INSERT INTO `job_task` (`job_id`, `task_id`, `execution_order`) VALUES
                                                                    (1, 1, 1), (1, 2, 2), (1, 3, 3), (1, 4, 4), (1, 5, 5),
                                                                    (2, 6, 1), (2, 7, 2);

-- 스케줄 설정 (매일 오전 8시)
INSERT INTO `schedule` (`workflow_id`, `cron_expression`, `is_active`) VALUES
    (1, '0 0 8 * * ?', TRUE);