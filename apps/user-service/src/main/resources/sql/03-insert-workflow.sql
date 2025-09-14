-- 워크플로우 관련 데이터 삽입

-- 카테고리 삽입
INSERT INTO `category` (`name`, `description`) VALUES
                                                   ('마케팅', '마케팅 관련 자동화 워크플로우'),
                                                   ('콘텐츠', '콘텐츠 생성 및 관리'),
                                                   ('데이터 수집', '웹 크롤링 및 데이터 수집 관련');

-- 워크플로우 생성
INSERT INTO `workflow` (`name`, `description`, `is_enabled`, `created_by`) VALUES
    ('트렌드_블로그_자동화', '트렌드 검색부터 블로그 글 작성까지 전체 자동화 프로세스', TRUE, 1);

-- Job 생성
INSERT INTO `job` (`name`, `description`, `is_enabled`, `created_by`) VALUES
                                                                          ('트렌드_검색_작업', '최신 트렌드 키워드 검색 및 분석', TRUE, 1),
                                                                          ('싸다구_크롤링_작업', '싸다구 사이트에서 관련 상품 정보 크롤링', TRUE, 1),
                                                                          ('블로그_글_작성_작업', '수집된 데이터를 바탕으로 블로그 글 자동 생성', TRUE, 1);

-- Task 생성
INSERT INTO `task` (`name`, `type`, `parameters`) VALUES
-- 트렌드 검색 관련 태스크
('구글_트렌드_검색', 'API_CALL', JSON_OBJECT(
        'api_endpoint', 'https://trends.googleapis.com/trends/api',
        'search_region', 'KR',
        'timeframe', 'now 7-d',
        'category', '0'
                          )),
('네이버_트렌드_검색', 'API_CALL', JSON_OBJECT(
        'api_endpoint', 'https://datalab.naver.com/keyword/trendSearch.naver',
        'period', 'week',
        'device', 'pc'
                           )),
('키워드_분석_및_필터링', 'DATA_PROCESSING', JSON_OBJECT(
        'min_score', 50,
        'max_keywords', 10,
        'filter_rules', JSON_ARRAY('adult_content', 'spam_keywords')
                                    )),

-- 싸다구 크롤링 관련 태스크
('싸다구_상품_검색', 'WEB_SCRAPING', JSON_OBJECT(
        'base_url', 'https://www.ssg.com',
        'search_path', '/search.ssg',
        'max_pages', 3,
        'delay_ms', 2000
                              )),
('상품_정보_추출', 'DATA_EXTRACTION', JSON_OBJECT(
        'extract_fields', JSON_ARRAY('title', 'price', 'rating', 'review_count', 'image_url'),
        'data_validation', true
                                )),
('가격_비교_분석', 'DATA_ANALYSIS', JSON_OBJECT(
        'comparison_sites', JSON_ARRAY('쿠팡', '11번가', '옥션'),
        'price_threshold', 0.1
                              )),

-- 블로그 글 작성 관련 태스크
('블로그_템플릿_선택', 'TEMPLATE_PROCESSING', JSON_OBJECT(
        'template_type', 'product_review',
        'style', 'conversational',
        'target_length', 1500
                                      )),
('AI_콘텐츠_생성', 'AI_GENERATION', JSON_OBJECT(
        'model', 'gpt-4',
        'temperature', 0.7,
        'max_tokens', 2000,
        'prompt_template', '트렌드 키워드와 상품 정보를 바탕으로 자연스러운 블로그 글을 작성해주세요.'
                               )),
('콘텐츠_검수_및_최적화', 'CONTENT_REVIEW', JSON_OBJECT(
        'seo_optimization', true,
        'readability_check', true,
        'plagiarism_check', true
                                   )),
('블로그_플랫폼_발행', 'PUBLISHING', JSON_OBJECT(
        'platforms', JSON_ARRAY('네이버 블로그', '티스토리', '브런치'),
        'schedule_publish', false,
        'auto_tags', true
                             ));

-- 워크플로우-Job 연결
INSERT INTO `workflow_job` (`workflow_id`, `job_id`) VALUES
                                                         (1, 1),  -- 트렌드_블로그_자동화 + 트렌드_검색_작업
                                                         (1, 2),  -- 트렌드_블로그_자동화 + 싸다구_크롤링_작업
                                                         (1, 3);  -- 트렌드_블로그_자동화 + 블로그_글_작성_작업

-- Job-Task 연결 (실행 순서 포함)
-- 트렌드 검색 작업의 태스크들
INSERT INTO `job_task` (`job_id`, `task_id`, `execution_order`) VALUES
                                                                    (1, 1, 1),  -- 구글_트렌드_검색
                                                                    (1, 2, 2),  -- 네이버_트렌드_검색
                                                                    (1, 3, 3);  -- 키워드_분석_및_필터링

-- 싸다구 크롤링 작업의 태스크들
INSERT INTO `job_task` (`job_id`, `task_id`, `execution_order`) VALUES
                                                                    (2, 4, 1),  -- 싸다구_상품_검색
                                                                    (2, 5, 2),  -- 상품_정보_추출
                                                                    (2, 6, 3);  -- 가격_비교_분석

-- 블로그 글 작성 작업의 태스크들
INSERT INTO `job_task` (`job_id`, `task_id`, `execution_order`) VALUES
                                                                    (3, 7, 1),  -- 블로그_템플릿_선택
                                                                    (3, 8, 2),  -- AI_콘텐츠_생성
                                                                    (3, 9, 3),  -- 콘텐츠_검수_및_최적화
                                                                    (3, 10, 4); -- 블로그_플랫폼_발행

-- 스케줄 설정 (매일 오전 8시 실행)
INSERT INTO `schedule` (`workflow_id`, `cron_expression`, `parameters`, `is_active`, `created_by`) VALUES
    (1, '0 0 8 * * *', JSON_OBJECT(
            'timezone', 'Asia/Seoul',
            'retry_count', 3,
            'timeout_minutes', 60,
            'notification_email', 'admin@icebang.site'
                     ), TRUE, 1);

-- 사용자별 설정 (관리자용)
INSERT INTO `user_config` (`user_id`, `type`, `name`, `json`, `is_active`) VALUES
    (1, 'workflow_preference', '트렌드_블로그_설정', JSON_OBJECT(
            'preferred_keywords', JSON_ARRAY('테크', 'IT', '트렌드', '리뷰'),
            'blog_style', 'casual',
            'auto_publish', false,
            'notification_enabled', true
                                             ), TRUE);