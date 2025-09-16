from datetime import datetime
from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field, HttpUrl


# 기본 요청
class RequestBase(BaseModel):
    pass


# 기본 응답
class ResponseBase(BaseModel):
    status: str = Field(..., title="상태", description="요청 처리 상태")
    pass


# 네이버 키워드 추출
class RequestNaverSearch(RequestBase):
    tag: str = Field(..., title="태그", description="데이터랩/스토어 태그 구분")
    category: Optional[str] = Field(
        None, title="카테고리", description="검색할 카테고리"
    )
    start_date: Optional[str] = Field(
        None, title="시작일", description="검색 시작 날짜 (YYYY-MM-DD)"
    )
    end_date: Optional[str] = Field(
        None, title="종료일", description="검색 종료 날짜 (YYYY-MM-DD)"
    )


class ResponseNaverSearch(ResponseBase):
    category: Optional[str] = Field(None, title="카테고리", description="검색 카테고리")
    keyword: str = Field(..., title="키워드", description="검색에 사용된 키워드")
    total_keyword: Dict[int, str] = Field(
        ..., title="총 키워드", description="키워드별 총 검색 결과"
    )


# 2단계: 검색
class RequestSadaguSearch(RequestBase):
    keyword: str = Field(..., title="검색 키워드", description="상품을 검색할 키워드")


class ResponseSadaguSearch(ResponseBase):
    keyword: str = Field(..., title="검색 키워드", description="검색에 사용된 키워드")
    search_results: List[Dict] = Field(
        ..., title="검색 결과", description="검색된 상품 목록"
    )


# 3단계: 매칭
class RequestSadaguMatch(RequestBase):
    keyword: str = Field(..., title="매칭 키워드", description="상품과 매칭할 키워드")
    search_results: List[Dict] = Field(
        ..., title="검색 결과", description="이전 단계에서 검색된 상품 목록"
    )


class ResponseSadaguMatch(ResponseBase):
    keyword: str = Field(..., title="매칭 키워드", description="매칭에 사용된 키워드")
    matched_products: List[Dict] = Field(
        ..., title="매칭된 상품", description="키워드와 매칭된 상품 목록"
    )


# 4단계: 유사도
class RequestSadaguSimilarity(RequestBase):
    keyword: str = Field(
        ..., title="유사도 분석 키워드", description="유사도 분석할 키워드"
    )
    matched_products: List[Dict] = Field(
        ..., title="매칭된 상품", description="이전 단계에서 매칭된 상품 목록"
    )
    search_results: Optional[List[Dict]] = Field(
        None,
        title="검색 결과",
        description="매칭 실패시 사용할 전체 검색 결과 (폴백용)",
    )


class ResponseSadaguSimilarity(ResponseBase):
    keyword: str = Field(
        ..., title="분석 키워드", description="유사도 분석에 사용된 키워드"
    )
    selected_product: Optional[Dict] = Field(
        None, title="선택된 상품", description="유사도 분석 결과 선택된 상품"
    )
    reason: Optional[str] = Field(
        None, title="선택 이유", description="상품 선택 근거 및 점수 정보"
    )


# 사다구몰 크롤링
class RequestSadaguCrawl(RequestBase):
    tag: str = Field(
        ...,
        title="크롤링 태그",
        description="크롤링 유형을 구분하는 태그 (예: 'detail')",
    )
    product_url: HttpUrl = Field(
        ..., title="상품 URL", description="크롤링할 상품 페이지의 URL"
    )


class ResponseSadaguCrawl(ResponseBase):
    tag: str = Field(..., title="크롤링 태그", description="크롤링 유형 태그")
    product_url: str = Field(..., title="상품 URL", description="크롤링된 상품 URL")
    product_detail: Optional[Dict] = Field(
        None, title="상품 상세정보", description="크롤링된 상품의 상세 정보"
    )
    crawled_at: Optional[str] = Field(
        None, title="크롤링 시간", description="크롤링 완료 시간"
    )


# 블로그 콘텐츠 생성
class RequestBlogCreate(RequestBase):
    pass


class ResponseBlogCreate(ResponseBase):
    pass


# 블로그 배포
class RequestBlogPublish(RequestBase):
    tag: str = Field(..., title="블로그 태그", description="블로그 플랫폼 종류")
    blog_id: str = Field(..., description="블로그 아이디")
    blog_pw: str = Field(..., description="블로그 비밀번호")
    post_title: str = Field(..., description="포스팅 제목")
    post_content: str = Field(..., description="포스팅 내용")
    post_tags: List[str] = Field(default=[], description="포스팅 태그 목록")


class ResponseBlogPublish(ResponseBase):
    # 디버깅 용
    metadata: Optional[Dict[str, Any]] = Field(
        None, description="포스팅 관련 메타데이터"
    )

    # 프로덕션 용
    # post_url: str = Field(..., description="포스팅 URL")
