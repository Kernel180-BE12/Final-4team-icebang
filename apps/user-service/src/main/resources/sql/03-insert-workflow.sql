-- ===================================================================
-- 워크플로우 관련 데이터 초기화
-- ===================================================================
-- 참조 관계 역순으로 데이터 삭제
DELETE FROM `schedule`;
DELETE FROM `job_task`;
DELETE FROM `workflow_job`;
DELETE FROM `task`;
DELETE FROM `job`;
DELETE FROM `workflow`;

-- ===================================================================
-- 워크플로우 정적 데이터 삽입
-- ===================================================================

-- 워크플로우 생성 (ID: 1)
INSERT INTO `workflow` (`id`, `name`, `description`, `created_by`, `default_config`) VALUES
    (1, '상품 분석 및 블로그 자동 발행', '키워드 검색부터 상품 분석 후 블로그 발행까지의 자동화 프로세스', 1,
     JSON_OBJECT('1',json_object('tag','naver'),'9',json_object('tag','blogger','blog_id', '', 'blog_pw', '')))
ON DUPLICATE KEY UPDATE
                     name = VALUES(name),
                     description = VALUES(description),
                     updated_at = NOW();
-- Job 생성 (ID: 1, 2)
INSERT INTO `job` (`id`, `name`, `description`, `created_by`) VALUES
                                                                  (1, '상품 분석', '키워드 검색, 상품 크롤링 및 유사도 분석 작업', 1),
                                                                  (2, '블로그 콘텐츠 생성', '분석 데이터를 기반으로 RAG 콘텐츠 생성 및 발행 작업', 1)
    ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), updated_at = NOW();

-- Task 생성 (ID: 1 ~ 9)
INSERT INTO `task` (`id`, `name`, `type`, `parameters`) VALUES
                                                            (1, '키워드 검색 태스크', 'FastAPI', JSON_OBJECT(
                                                                    'endpoint', '/keywords/search', 'method', 'POST',
                                                                    'body', JSON_OBJECT('tag', 'String') -- { "tag": str }
                                                                                         )),
                                                            (2, '상품 검색 태스크', 'FastAPI', JSON_OBJECT(
                                                                    'endpoint', '/products/search', 'method', 'POST',
                                                                    'body', JSON_OBJECT('keyword', 'String') -- { "keyword": str }
                                                                                        )),
                                                            (3, '상품 매칭 태스크', 'FastAPI', JSON_OBJECT(
                                                                    'endpoint', '/products/match', 'method', 'POST',
                                                                    'body', JSON_OBJECT( -- { keyword: str, search_results: List }
                                                                            'keyword', 'String',
                                                                            'search_results', 'List'
                                                                            )
                                                                                        )),
                                                            (4, '상품 유사도 분석 태스크', 'FastAPI', JSON_OBJECT(
                                                                    'endpoint', '/products/similarity', 'method', 'POST',
                                                                    'body', JSON_OBJECT( -- { keyword: str, matched_products: List, search_results: List }
                                                                            'keyword', 'String',
                                                                            'matched_products', 'List',
                                                                            'search_results', 'List'
                                                                            )
                                                                                            )),
                                                            (5, '상품 정보 크롤링 태스크', 'FastAPI', JSON_OBJECT(
                                                                    'endpoint', '/products/crawl', 'method', 'POST',
                                                                    'body', JSON_OBJECT('product_urls', 'List') -- { "product_urls": List[str] } 수정됨
                                                                                            )),
                                                            (6, 'S3 업로드 태스크', 'FastAPI', JSON_OBJECT(
                                                                    'endpoint', '/products/s3-upload', 'method', 'POST',
                                                                    'body', JSON_OBJECT( -- { keyword: str, crawled_products: List, base_folder: str }
                                                                            'keyword', 'String',
                                                                            'crawled_products', 'List',
                                                                            'base_folder', 'String'
                                                                            )
                                                                                         )),
                                                            (7, '상품 선택 태스크', 'FastAPI', JSON_OBJECT(
                                                                    'endpoint', '/products/select', 'method', 'POST',
                                                                    'body', JSON_OBJECT( -- { task_run_id: int, selection_criteria: str }
                                                                            'task_run_id', 'Integer',
                                                                            'selection_criteria', 'String'
                                                                            )
                                                                                        )),
                                                            -- RAG관련 request body는 추후에 결정될 예정
                                                            (8, '블로그 RAG 생성 태스크', 'FastAPI', JSON_OBJECT('endpoint', '/blogs/rag/create', 'method', 'POST')),
                                                            (9, '블로그 발행 태스크', 'FastAPI', JSON_OBJECT(
                                                                    'endpoint', '/blogs/publish', 'method', 'POST',
                                                                    'body', JSON_OBJECT( -- { tag: str, blog_id: str, ... }
                                                                            'tag', 'String',
                                                                            'blog_id', 'String',
                                                                            'blog_pw', 'String',
                                                                            'blog_name', 'String',
                                                                            'post_title', 'String',
                                                                            'post_content', 'String',
                                                                            'post_tags', 'List'
                                                                            )
                                                                                         ))
    ON DUPLICATE KEY UPDATE name = VALUES(name), type = VALUES(type), parameters = VALUES(parameters), updated_at = NOW();

-- ===================================================================
-- 워크플로우 구조 및 스케줄 데이터 삽입
-- ===================================================================
-- 워크플로우-Job 연결
INSERT INTO `workflow_job` (`workflow_id`, `job_id`, `execution_order`) VALUES
                                                                            (1, 1, 1),
                                                                            (1, 2, 2)
    ON DUPLICATE KEY UPDATE execution_order = VALUES(execution_order);

-- Job-Task 연결
INSERT INTO `job_task` (`job_id`, `task_id`, `execution_order`) VALUES
                                                                    -- Job 1: 상품 분석 (키워드검색 → 상품검색 → 매칭 → 유사도 → 크롤링 → S3업로드 → 상품선택)
                                                                    (1, 1, 1), (1, 2, 2), (1, 3, 3), (1, 4, 4), (1, 5, 5), (1, 6, 6), (1, 7, 7),
                                                                    -- Job 2: 블로그 콘텐츠 생성 (RAG생성 → 발행)
                                                                    (2, 8, 1), (2, 9, 2)
    ON DUPLICATE KEY UPDATE execution_order = VALUES(execution_order);

-- 스케줄 설정 (매일 오전 8시)
INSERT INTO `schedule` (`workflow_id`, `cron_expression`, `is_active`, `created_by`) VALUES
    (1, '0 0 8 * * ?', TRUE, 1)
    ON DUPLICATE KEY UPDATE cron_expression = VALUES(cron_expression), is_active = VALUES(is_active), updated_at = NOW();