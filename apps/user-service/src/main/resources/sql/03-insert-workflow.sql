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

-- Task 생성 (ID: 1 ~ 7)
INSERT INTO `task` (`id`, `name`, `type`, `parameters`) VALUES
-- Job 1의 Task들
                                                            (1, '키워드 검색 태스크', 'HTTP', JSON_OBJECT(
                                                                    'url', 'http://127.0.0.1:8000/keywords/search',
                                                                    'method', 'POST',
                                                                    'body', JSON_OBJECT('tag', 'naver')
                                                                                      )),
                                                            (2, '상품 검색 태스크', 'HTTP', JSON_OBJECT(
                                                                    'url', 'http://127.0.0.1:8000/products/search',
                                                                    'method', 'POST',
                                                                    'input_mapping', JSON_OBJECT(
                                                                            'keyword', '키워드 검색 태스크.data.keyword'
                                                                                     )
                                                                                     )),
                                                            (3, '상품 매칭 태스크', 'HTTP', JSON_OBJECT(
                                                                    'url', 'http://127.0.0.1:8000/products/match',
                                                                    'method', 'POST',
                                                                    'input_mapping', JSON_OBJECT(
                                                                            'keyword', '키워드 검색 태스크.data.keyword',
                                                                            'search_results', '상품 검색 태스크.data.search_results'
                                                                                     )
                                                                                     )),
                                                            (4, '상품 유사도 분석 태스크', 'HTTP', JSON_OBJECT(
                                                                    'url', 'http://127.0.0.1:8000/products/similarity',
                                                                    'method', 'POST',
                                                                    'input_mapping', JSON_OBJECT(
                                                                            'keyword', '키워드 검색 태스크.data.keyword',
                                                                            'matched_products', '상품 매칭 태스크.data.matched_products'
                                                                                     )
                                                                                         )),
                                                            (5, '상품 정보 크롤링 태스크', 'HTTP', JSON_OBJECT(
                                                                    'url', 'http://127.0.0.1:8000/products/crawl',
                                                                    'method', 'POST',
                                                                    'input_mapping', JSON_OBJECT(
                                                                            'product_url', '상품 유사도 분석 태스크.data.selected_product.url'
                                                                                     )
                                                                                         )),

                                                            -- Job 2의 Task들
                                                            (6, '블로그 RAG 생성 태스크', 'HTTP', JSON_OBJECT(
                                                                    'url', 'http://127.0.0.1:8000/blogs/rag/create',
                                                                    'method', 'POST',
                                                                    'input_mapping', JSON_OBJECT(
                                                                            'keyword', '키워드 검색 태스크.data.keyword',
                                                                            'product_info', '상품 정보 크롤링 태스크.data.product_detail'
                                                                                     ))),

-- Task 7 설정 확인 필요
                                                            (7, '블로그 발행 태스크', 'HTTP', JSON_OBJECT(
                                                                    'url', 'http://127.0.0.1:8000/blogs/publish',
                                                                    'method', 'POST',
                                                                    'body', JSON_OBJECT('tag', 'tistory', 'blog_id', 'test', 'blog_pw', 'test'),
                                                                    'input_mapping', JSON_OBJECT(
                                                                            'post_title', '블로그 RAG 생성 태스크.data.title',
                                                                            'post_content', '블로그 RAG 생성 태스크.data.content',
                                                                            'post_tags', '블로그 RAG 생성 태스크.data.tags'
                                                                                     )));

-- 워크플로우-Job 연결
INSERT INTO `workflow_job` (`workflow_id`, `job_id`, `execution_order`) VALUES
                                                                            (1, 1, 1),
                                                                            (1, 2, 2);

-- Job-Task 연결
INSERT INTO `job_task` (`job_id`, `task_id`, `execution_order`) VALUES
                                                                    (1, 1, 1), (1, 2, 2), (1, 3, 3), (1, 4, 4), (1, 5, 5),
                                                                    (2, 6, 1), (2, 7, 2);

-- 스케줄 설정 (매분 0초마다 실행)
INSERT INTO `schedule` (`workflow_id`, `cron_expression`, `is_active`) VALUES
    (1, '0 * * * * ?', TRUE);