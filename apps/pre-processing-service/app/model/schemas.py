from datetime import datetime
from typing import Optional, List, Dict, Any, TypeVar, Generic
from pydantic import BaseModel, Field, HttpUrl

# 제네릭 타입 변수 정의
T = TypeVar('T')


# 기본 요청
class RequestBase(BaseModel):
    pass


# 기본 응답
class ResponseBase(BaseModel, Generic[T]):
    success: bool = Field(..., title="성공유무", description="true,false")
    data: T = Field(..., title="응답 데이터")
    status: str = Field(..., title="상태", description="요청 처리 상태")
    message: str = Field(..., title="메시지", description="메시지입니다.")


# ============== 1단계: 네이버 키워드 추출 ==============

class RequestNaverSearch(RequestBase):
    tag: str = Field(..., title="태그", description="데이터랩/스토어 태그 구분")


# 응답 데이터 모델
class NaverSearchData(BaseModel):
    keyword: str = Field(..., title="키워드", description="검색에 사용된 키워드")
    total_keyword: Dict[int, str] = Field(
        ..., title="총 키워드", description="키워드별 총 검색 결과"
    )


# 최종 응답 모델
class ResponseNaverSearch(ResponseBase[NaverSearchData]):
    """네이버 키워드 검색 API 응답"""
    pass


# ============== 2단계: 사다구 검색 ==============

class RequestSadaguSearch(RequestBase):
    keyword: str = Field(..., title="검색 키워드", description="상품을 검색할 키워드")


# 응답 데이터 모델
class SadaguSearchData(BaseModel):
    keyword: str = Field(..., title="검색 키워드", description="검색에 사용된 키워드")
    search_results: List[Dict] = Field(
        ..., title="검색 결과", description="검색된 상품 목록"
    )

# 최종 응답 모델
class ResponseSadaguSearch(ResponseBase[SadaguSearchData]):
    """사다구 상품 검색 API 응답"""
    pass


# ============== 3단계: 사다구 매칭 ==============

class RequestSadaguMatch(RequestBase):
    keyword: str = Field(..., title="매칭 키워드", description="상품과 매칭할 키워드")
    search_results: List[Dict] = Field(
        ..., title="검색 결과", description="이전 단계에서 검색된 상품 목록"
    )


# 응답 데이터 모델
class SadaguMatchData(BaseModel):
    keyword: str = Field(..., title="매칭 키워드", description="매칭에 사용된 키워드")
    matched_products: List[Dict] = Field(
        ..., title="매칭된 상품", description="키워드와 매칭된 상품 목록"
    )
    match_count: Optional[int] = Field(None, title="매칭 결과 수")
    match_score: Optional[float] = Field(None, title="전체 매칭 점수")


# 최종 응답 모델
class ResponseSadaguMatch(ResponseBase[SadaguMatchData]):
    """사다구 상품 매칭 API 응답"""
    pass


# ============== 4단계: 사다구 유사도 ==============

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


# 응답 데이터 모델
class SadaguSimilarityData(BaseModel):
    keyword: str = Field(
        ..., title="분석 키워드", description="유사도 분석에 사용된 키워드"
    )
    selected_product: Optional[Dict] = Field(
        None, title="선택된 상품", description="유사도 분석 결과 선택된 상품"
    )
    reason: Optional[str] = Field(
        None, title="선택 이유", description="상품 선택 근거 및 점수 정보"
    )
    similarity_score: Optional[float] = Field(None, title="유사도 점수")
    analyzed_count: Optional[int] = Field(None, title="분석된 상품 수")


# 최종 응답 모델
class ResponseSadaguSimilarity(ResponseBase[SadaguSimilarityData]):
    """사다구 상품 유사도 분석 API 응답"""
    pass


# ============== 사다구몰 크롤링 ==============

class RequestSadaguCrawl(RequestBase):
    tag: str = Field(
        ...,
        title="크롤링 태그",
        description="크롤링 유형을 구분하는 태그 (예: 'detail')",
    )
    product_url: HttpUrl = Field(
        ..., title="상품 URL", description="크롤링할 상품 페이지의 URL"
    )


# 응답 데이터 모델
class SadaguCrawlData(BaseModel):
    tag: str = Field(..., title="크롤링 태그", description="크롤링 유형 태그")
    product_url: str = Field(..., title="상품 URL", description="크롤링된 상품 URL")
    product_detail: Optional[Dict] = Field(
        None, title="상품 상세정보", description="크롤링된 상품의 상세 정보"
    )
    crawled_at: Optional[str] = Field(
        None, title="크롤링 시간", description="크롤링 완료 시간"
    )
    crawl_success: Optional[bool] = Field(None, title="크롤링 성공 여부")
    crawl_duration_ms: Optional[int] = Field(None, title="크롤링 소요 시간(ms)")


# 최종 응답 모델
class ResponseSadaguCrawl(ResponseBase[SadaguCrawlData]):
    """사다구몰 크롤링 API 응답"""
    pass


# ============== 블로그 콘텐츠 생성 ==============

class RequestBlogCreate(RequestBase):
    keyword: Optional[str] = Field(None, title="키워드", description="콘텐츠 생성용 키워드")
    product_info: Optional[Dict] = Field(None, title="상품 정보", description="블로그 콘텐츠에 포함할 상품 정보")
    content_type: Optional[str] = Field(None, title="콘텐츠 타입", description="생성할 콘텐츠 유형")
    target_length: Optional[int] = Field(None, title="목표 글자 수", description="생성할 콘텐츠의 목표 길이")


# 응답 데이터 모델
class BlogCreateData(BaseModel):
    title: str = Field(..., title="블로그 제목", description="생성된 블로그 제목")
    content: str = Field(..., title="블로그 내용", description="생성된 블로그 내용")
    tags: List[str] = Field(default_factory=list, title="추천 태그", description="콘텐츠에 적합한 태그 목록")
    word_count: Optional[int] = Field(None, title="글자 수", description="생성된 콘텐츠의 글자 수")
    generation_time_ms: Optional[int] = Field(None, title="생성 소요 시간(ms)")


# 최종 응답 모델
class ResponseBlogCreate(ResponseBase[BlogCreateData]):
    """블로그 콘텐츠 생성 API 응답"""
    pass


# ============== 블로그 배포 ==============

class RequestBlogPublish(RequestBase):
    tag: str = Field(..., title="블로그 태그", description="블로그 플랫폼 종류")
    blog_id: str = Field(..., description="블로그 아이디")
    blog_pw: str = Field(..., description="블로그 비밀번호")
    post_title: str = Field(..., description="포스팅 제목")
    post_content: str = Field(..., description="포스팅 내용")
    post_tags: List[str] = Field(default_factory=list, description="포스팅 태그 목록")


# 응답 데이터 모델
class BlogPublishData(BaseModel):
    platform: str = Field(..., title="블로그 플랫폼", description="배포된 블로그 플랫폼")
    post_title: str = Field(..., title="포스팅 제목", description="배포된 포스팅 제목")
    post_url: Optional[str] = Field(None, title="포스팅 URL", description="배포된 포스팅 URL")
    published_at: Optional[str] = Field(None, title="배포 시간", description="포스팅 배포 완료 시간")
    publish_success: bool = Field(..., title="배포 성공 여부")

    # 디버깅 용 (Optional로 변경)
    metadata: Optional[Dict[str, Any]] = Field(
        None, description="포스팅 관련 메타데이터"
    )


# 최종 응답 모델
class ResponseBlogPublish(ResponseBase[BlogPublishData]):
    """블로그 배포 API 응답"""
    pass


# # ============== 공통 에러 응답 ==============
#
# class ErrorData(BaseModel):
#     error_code: str = Field(..., title="에러 코드")
#     error_detail: str = Field(..., title="에러 상세")
#     timestamp: str = Field(..., title="발생 시각")
#     trace_id: Optional[str] = Field(None, title="추적 ID")
#
#
# class ResponseError(ResponseBase[ErrorData]):
#     """공통 에러 응답"""
#     pass
#
#
# # ============== 응답 헬퍼 함수 ==============
#
# def create_success_response(
#         data: T,
#         message: str = "성공",
#         status: str = "200"
# ) -> ResponseBase[T]:
#     """성공 응답 생성 헬퍼"""
#     return ResponseBase[T](
#         success=True,
#         data=data,
#         status=status,
#         message=message
#     )
#
#
# def create_error_response(
#         error_code: str,
#         error_detail: str,
#         status: str = "500",
#         trace_id: Optional[str] = None
# ) -> ResponseError:
#     """에러 응답 생성 헬퍼"""
#     error_data = ErrorData(
#         error_code=error_code,
#         error_detail=error_detail,
#         timestamp=datetime.now().isoformat(),
#         trace_id=trace_id
#     )
#
#     return ResponseError(
#         success=False,
#         data=error_data,
#         status=status,
#         message="요청 처리 중 오류가 발생했습니다"
#     )
