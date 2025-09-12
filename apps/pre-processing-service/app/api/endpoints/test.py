# app/api/endpoints/embedding.py
import loguru
from fastapi import APIRouter
from sqlalchemy import text

from app.decorators.logging import log_api_call
from ...errors.CustomException import *
from ...errors.BlogPostingException import *
from fastapi import APIRouter
from typing import Mapping, Any, Dict
from ...model.schemas import *
from app.service.blog.blog_post_service_factory import BlogPostServiceFactory
from ...service.keyword_service import keyword_search
from ...service.match_service import MatchService
from ...service.search_service import SearchService

# from ...service.similarity_service import SimilarityService
from ...db.db_connecter import engine  # ✅ 우리가 만든 DB 유틸 임포트

# 이 파일만의 독립적인 라우터를 생성합니다.
router = APIRouter()


@router.get("/")
async def root():
    return {"message": "테스트 API"}


@router.get("/hello/{name}", tags=["hello"])
# @log_api_call
async def say_hello(name: str):
    return {"message": f"Hello {name}"}


# 특정 경로에서 의도적으로 에러 발생
# 커스텀에러 테스터 url
@router.get("/error/{item_id}")
async def trigger_error(item_id: int):
    if item_id == 0:
        raise InvalidItemDataException()

    if item_id == 404:
        raise ItemNotFoundException(item_id=item_id)

    if item_id == 500:
        raise ValueError("이것은 테스트용 값 오류입니다.")

    return {"result": item_id}


@router.get("/db-test", tags=["db"])
async def db_test():
    """간단한 DB 연결 및 쿼리 테스트"""
    try:
        with engine.connect() as conn:
            result = conn.execute(text("SELECT NOW() as now"))
            row = result.fetchone()
            return {"status": "ok", "db_time": str(row.now)}
    except Exception as e:
        return {"status": "error", "detail": str(e)}


def with_meta(data: Mapping[str, Any], meta: Mapping[str, Any]) -> Dict[str, Any]:
    """요청 payload + 공통 meta 머지"""
    return {**meta, **data}


@router.get("/tester", response_model=None)
async def processing_tester():
    meta = {
        "job_id": 1,
        "schedule_id": 1,
        "schedule_his_id": 1,
    }
    request_dict = {
        "tag": "naver",
        "category": "50000000",
        "start_date": "2025-09-01",
        "end_date": "2025-09-02",
    }

    try:
        # 네이버 키워드 검색
        naver_request = RequestNaverSearch(**with_meta(meta, request_dict))
        response_data = await keyword_search(naver_request)
        keyword = response_data.get("keyword")
        loguru.logger.info(f"검색된 키워드: {keyword}")

        keyword = {
            "keyword": keyword,
        }

        # 싸다구 상품 검색
        sadagu_request = RequestSadaguSearch(**with_meta(meta, keyword))
        search_service = SearchService()
        keyword_result = await search_service.search_products(sadagu_request)
        loguru.logger.info(f"상품 검색 결과: {keyword_result}")

        # 싸다구 상품 매치
        keyword["search_results"] = keyword_result.get("search_results")
        keyword_match_request = RequestSadaguMatch(**with_meta(meta, keyword))
        match_service = MatchService()
        keyword_match_response = match_service.match_products(keyword_match_request)
        loguru.logger.info(f"상품 매칭 결과: {keyword_match_response}")

        # 싸다구 상품 유사도 분석
        keyword["matched_products"] = keyword_match_response.get("matched_products")
        keyword_similarity_request = RequestSadaguSimilarity(**with_meta(meta, keyword))
        # similarity_service = SimilarityService()
        # keyword_similarity_response = similarity_service.select_product_by_similarity(
        #     keyword_similarity_request
        # )
        # loguru.logger.info(keyword_similarity_response)

        # 싸다구 상품 크롤링

        # 블로그 생성

        # 블로그 배포 - Factory 패턴 사용
        blog_service = BlogPostServiceFactory.create_service("tistory")
        result = blog_service.post_content(
            title="안녕하살법",
            content="안녕하살법 받아치기러기 코드 받아치기",
            tags=["퉁퉁퉁사후르", "짜라짜라"],
        )
        loguru.logger.info(f"블로그 포스팅 결과: {result}")

        return {
            "status": "success",
            "keyword": keyword,
            "blog_result": result,
            "message": "전체 파이프라인 처리 완료"
        }

    except ValueError as e:
        loguru.logger.error(f"지원하지 않는 플랫폼 오류: {e}")
        return {"status": "error", "detail": f"지원하지 않는 플랫폼: {str(e)}"}

    except BlogLoginException as e:
        loguru.logger.error(f"블로그 로그인 실패: {e.detail}")
        return {"status": "error", "detail": e.detail}

    except BlogContentValidationException as e:
        loguru.logger.error(f"콘텐츠 유효성 검사 실패: {e.detail}")
        return {"status": "error", "detail": e.detail}

    except BlogPostPublishException as e:
        loguru.logger.error(f"블로그 포스트 발행 실패: {e.detail}")
        return {"status": "error", "detail": e.detail}

    except BlogServiceUnavailableException as e:
        loguru.logger.error(f"블로그 서비스 이용 불가: {e.detail}")
        return {"status": "error", "detail": e.detail}

    except BlogServiceInitializationException as e:
        loguru.logger.error(f"블로그 서비스 초기화 실패: {e.detail}")
        return {"status": "error", "detail": e.detail}

    except Exception as e:
        loguru.logger.error(f"예상치 못한 오류: {str(e)}")
        return {"status": "error", "detail": f"예상치 못한 오류가 발생했습니다: {str(e)}"}