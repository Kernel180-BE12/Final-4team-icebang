# -- ===================================================================
# -- 기존 서버 데이터의 시간대 보정 (KST → UTC 변환)
# -- ===================================================================
# -- 이 스크립트는 서버에 올라가 있는 기존 더미데이터들의 시간을 UTC로 변환합니다.
# -- 한국시간(KST, +09:00)으로 저장된 데이터를 UTC(+00:00)로 변환
#
# -- ===================================================================
# -- 1. 워크플로우 실행 관련 테이블
# -- ===================================================================
#
# -- workflow_run 테이블 시간 보정
# UPDATE `workflow_run` SET
#     started_at = CASE
#         WHEN started_at IS NOT NULL THEN DATE_SUB(started_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     finished_at = CASE
#         WHEN finished_at IS NOT NULL THEN DATE_SUB(finished_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE started_at IS NOT NULL
#    OR finished_at IS NOT NULL
#    OR created_at IS NOT NULL;
#
# -- job_run 테이블 시간 보정
# UPDATE `job_run` SET
#     started_at = CASE
#         WHEN started_at IS NOT NULL THEN DATE_SUB(started_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     finished_at = CASE
#         WHEN finished_at IS NOT NULL THEN DATE_SUB(finished_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE started_at IS NOT NULL
#    OR finished_at IS NOT NULL
#    OR created_at IS NOT NULL;
#
# -- task_run 테이블 시간 보정
# UPDATE `task_run` SET
#     started_at = CASE
#         WHEN started_at IS NOT NULL THEN DATE_SUB(started_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     finished_at = CASE
#         WHEN finished_at IS NOT NULL THEN DATE_SUB(finished_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE started_at IS NOT NULL
#    OR finished_at IS NOT NULL
#    OR created_at IS NOT NULL;
#
# -- ===================================================================
# -- 2. 마스터 데이터 테이블들
# -- ===================================================================
#
# -- workflow 테이블 시간 보정
# UPDATE `workflow` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     updated_at = CASE
#         WHEN updated_at IS NOT NULL THEN DATE_SUB(updated_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL
#    OR updated_at IS NOT NULL;
#
# -- job 테이블 시간 보정
# UPDATE `job` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     updated_at = CASE
#         WHEN updated_at IS NOT NULL THEN DATE_SUB(updated_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL
#    OR updated_at IS NOT NULL;
#
# -- task 테이블 시간 보정
# UPDATE `task` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     updated_at = CASE
#         WHEN updated_at IS NOT NULL THEN DATE_SUB(updated_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL
#    OR updated_at IS NOT NULL;
#
# -- schedule 테이블 시간 보정
# UPDATE `schedule` SET
#     last_run_at = CASE
#         WHEN last_run_at IS NOT NULL THEN DATE_SUB(last_run_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     updated_at = CASE
#         WHEN updated_at IS NOT NULL THEN DATE_SUB(updated_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE last_run_at IS NOT NULL
#    OR created_at IS NOT NULL
#    OR updated_at IS NOT NULL;
#
# -- ===================================================================
# -- 3. 사용자 관련 테이블들
# -- ===================================================================
#
# -- user 테이블 시간 보정
# UPDATE `user` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     updated_at = CASE
#         WHEN updated_at IS NOT NULL THEN DATE_SUB(updated_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     joined_at = CASE
#         WHEN joined_at IS NOT NULL THEN DATE_SUB(joined_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL
#    OR updated_at IS NOT NULL
#    OR joined_at IS NOT NULL;
#
# -- user_organization 테이블 시간 보정
# UPDATE `user_organization` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     updated_at = CASE
#         WHEN updated_at IS NOT NULL THEN DATE_SUB(updated_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL
#    OR updated_at IS NOT NULL;
#
# -- organization 테이블 시간 보정
# UPDATE `organization` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     updated_at = CASE
#         WHEN updated_at IS NOT NULL THEN DATE_SUB(updated_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL
#    OR updated_at IS NOT NULL;
#
# -- ===================================================================
# -- 4. 기타 시스템 테이블들
# -- ===================================================================
#
# -- permission 테이블 시간 보정
# UPDATE `permission` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     updated_at = CASE
#         WHEN updated_at IS NOT NULL THEN DATE_SUB(updated_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL
#    OR updated_at IS NOT NULL;
#
# -- execution_log 테이블 시간 보정
# UPDATE `execution_log` SET
#     executed_at = CASE
#         WHEN executed_at IS NOT NULL THEN DATE_SUB(executed_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     reserved5 = CASE
#         WHEN reserved5 IS NOT NULL THEN DATE_SUB(reserved5, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE executed_at IS NOT NULL
#    OR reserved5 IS NOT NULL;
#
# -- task_io_data 테이블 시간 보정
# UPDATE `task_io_data` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL;
#
# -- config 테이블 시간 보정
# UPDATE `config` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL;
#
# -- category 테이블 시간 보정
# UPDATE `category` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     updated_at = CASE
#         WHEN updated_at IS NOT NULL THEN DATE_SUB(updated_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL
#    OR updated_at IS NOT NULL;
#
# -- user_config 테이블 시간 보정
# UPDATE `user_config` SET
#     created_at = CASE
#         WHEN created_at IS NOT NULL THEN DATE_SUB(created_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END,
#     updated_at = CASE
#         WHEN updated_at IS NOT NULL THEN DATE_SUB(updated_at, INTERVAL 9 HOUR)
#         ELSE NULL
#     END
# WHERE created_at IS NOT NULL
#    OR updated_at IS NOT NULL;
#
# -- ===================================================================
# -- 완료 메시지
# -- ===================================================================
# -- 이 스크립트 실행 후 모든 시간 데이터가 UTC 기준으로 변환됩니다.
# -- 애플리케이션에서 Instant를 사용하여 UTC 시간으로 처리됩니다.