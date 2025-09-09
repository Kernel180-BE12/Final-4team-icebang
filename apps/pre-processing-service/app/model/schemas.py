from datetime import datetime
from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field, HttpUrl


# 기본 요청
class RequestBase(BaseModel):
    job_id: int
    schedule_id: int
    schedule_his_id: Optional[int] = None


# 기본 응답
class ResponseBase(BaseModel):
    job_id: int
    schedule_id: int
    schedule_his_id: Optional[int] = None
    status: str


# 네이버 키워드 추출
class RequestNaverSearch(RequestBase):
    tag: str
    category: Optional[str] = None
    start_date: Optional[str] = None
    end_date: Optional[str] = None


class ResponseNaverSearch(ResponseBase):
    category: Optional[str] = None
    keyword: str
    total_keyword: Dict[int, str]


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
class RequestSadaguCrawl(BaseModel):
    job_id: int = Field(
        ..., title="작업 ID", description="현재 실행중인 작업의 고유 식별자"
    )
    schedule_id: int = Field(
        ..., title="스케줄 ID", description="예약된 스케줄의 고유 식별자"
    )
    schedule_his_id: int = Field(
        ..., title="스케줄 히스토리 ID", description="스케줄 실행 이력의 고유 식별자"
    )
    tag: str = Field(
        ...,
        title="크롤링 태그",
        description="크롤링 유형을 구분하는 태그 (예: 'detail')",
    )
    product_url: HttpUrl = Field(
        ..., title="상품 URL", description="크롤링할 상품 페이지의 URL"
    )


class ResponseSadaguCrawl(BaseModel):
    job_id: int = Field(..., title="작업 ID", description="작업 식별자")
    schedule_id: int = Field(..., title="스케줄 ID", description="스케줄 식별자")
    schedule_his_id: int = Field(
        ..., title="스케줄 히스토리 ID", description="스케줄 이력 식별자"
    )
    tag: str = Field(..., title="크롤링 태그", description="크롤링 유형 태그")
    product_url: str = Field(..., title="상품 URL", description="크롤링된 상품 URL")
    product_detail: Optional[Dict] = Field(
        None, title="상품 상세정보", description="크롤링된 상품의 상세 정보"
    )
    status: str = Field(..., title="처리 상태", description="크롤링 처리 결과 상태")
    crawled_at: Optional[str] = Field(
        None, title="크롤링 시간", description="크롤링 완료 시간"
    )


# 블로그 생성
class RequestBlogCreate(RequestBase):
    tag: str
    category: str


class ResponseBlogCreate(ResponseBase):
    pass


# 블로그 배포
class RequestBlogPublish(RequestBase):
    tag: str
    category: str

    # 임의로 추가
    title: str
    content: str
    tags: List[str]


class ResponseBlogPublish(ResponseBase):
    metadata: Optional[Dict[str, Any]]
