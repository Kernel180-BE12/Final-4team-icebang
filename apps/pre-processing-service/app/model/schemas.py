from datetime import datetime
from typing import Optional, List, Dict, Any, TypeVar, Generic
from pydantic import BaseModel, Field, HttpUrl

# 제네릭 타입 변수 정의
T = TypeVar("T")


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
    top_products: List[Dict] = Field(
        default_factory=list,
        title="선택된 상품들",
        description="유사도 분석 결과 선택된 상위 상품 목록",
    )
    reason: Optional[str] = Field(
        None, title="선택 이유", description="상품 선택 근거 및 점수 정보"
    )


# 최종 응답 모델
class ResponseSadaguSimilarity(ResponseBase[SadaguSimilarityData]):
    """사다구 상품 유사도 분석 API 응답"""

    pass


# ============== 사다구몰 크롤링 ==============


class RequestSadaguCrawl(RequestBase):
    product_urls: List[HttpUrl] = Field(
        ..., title="상품 URL", description="크롤링할 상품 페이지의 URL"
    )


# 응답 데이터 모델
class SadaguCrawlData(BaseModel):
    crawled_products: List[Dict] = Field(
        ...,
        title="크롤링된 상품들",
        description="크롤링된 상품들의 상세 정보 목록 (URL 포함)",
    )
    success_count: int = Field(
        ..., title="성공 개수", description="성공적으로 크롤링된 상품 개수"
    )
    fail_count: int = Field(
        ..., title="실패 개수", description="크롤링에 실패한 상품 개수"
    )
    crawled_at: Optional[str] = Field(
        None, title="크롤링 시간", description="크롤링 완료 시간"
    )


# 최종 응답 모델
class ResponseSadaguCrawl(ResponseBase[SadaguCrawlData]):
    """사다구몰 크롤링 API 응답"""

    pass


# ============== S3 이미지 업로드 ==============


class RequestS3Upload(RequestBase):
    keyword: str = Field(
        ..., title="검색 키워드", description="폴더명 생성용 키워드"
    )  # 추가
    crawled_products: List[Dict] = Field(
        ...,
        title="크롤링된 상품 데이터",
        description="이전 단계에서 크롤링된 상품들의 데이터",
    )
    base_folder: Optional[str] = Field(
        "product", title="기본 폴더", description="S3 내 기본 저장 폴더 경로"
    )


# S3 업로드된 이미지 정보
class S3ImageInfo(BaseModel):
    index: int = Field(..., title="이미지 순번", description="상품 내 이미지 순번")
    original_url: str = Field(
        ..., title="원본 URL", description="크롤링된 원본 이미지 URL"
    )
    s3_url: str = Field(..., title="S3 URL", description="S3에서 접근 가능한 URL")


# 상품별 S3 업로드 결과
class ProductS3UploadResult(BaseModel):
    product_index: int = Field(..., title="상품 순번", description="크롤링 순번")
    product_title: str = Field(..., title="상품 제목", description="상품명")
    status: str = Field(..., title="업로드 상태", description="completed/skipped/error")
    uploaded_images: List[S3ImageInfo] = Field(
        default_factory=list, title="업로드 성공 이미지"
    )
    success_count: int = Field(
        ..., title="성공 개수", description="업로드 성공한 이미지 수"
    )
    fail_count: int = Field(
        ..., title="실패 개수", description="업로드 실패한 이미지 수"
    )


# S3 업로드 요약 정보
class S3UploadSummary(BaseModel):
    total_products: int = Field(
        ..., title="총 상품 수", description="처리 대상 상품 총 개수"
    )
    total_success_images: int = Field(
        ..., title="성공 이미지 수", description="업로드 성공한 이미지 총 개수"
    )
    total_fail_images: int = Field(
        ..., title="실패 이미지 수", description="업로드 실패한 이미지 총 개수"
    )


# 응답 데이터 모델
class S3UploadData(BaseModel):
    upload_results: List[ProductS3UploadResult] = Field(
        ..., title="업로드 결과", description="각 상품의 S3 업로드 결과"
    )
    summary: S3UploadSummary = Field(
        ..., title="업로드 요약", description="전체 업로드 결과 요약"
    )
    uploaded_at: str = Field(
        ..., title="업로드 완료 시간", description="S3 업로드 완료 시간"
    )
    # 🆕 임시: 콘텐츠 생성용 단일 상품만 추가 (나중에 삭제 예정)
    selected_product_for_content: Optional[Dict] = Field(
        None,
        title="콘텐츠 생성용 선택 상품",
        description="임시: 블로그 콘텐츠 생성을 위해 선택된 단일 상품 정보",
    )


# 최종 응답 모델
class ResponseS3Upload(ResponseBase[S3UploadData]):
    """S3 이미지 업로드 API 응답"""

    pass


# ============== 블로그 콘텐츠 생성 ==============


class RequestBlogCreate(RequestBase):
    keyword: Optional[str] = Field(
        None, title="키워드", description="콘텐츠 생성용 키워드"
    )
    product_info: Optional[Dict] = Field(
        None, title="상품 정보", description="블로그 콘텐츠에 포함할 상품 정보"
    )
    content_type: Optional[str] = Field(
        None, title="콘텐츠 타입", description="생성할 콘텐츠 유형"
    )
    target_length: Optional[int] = Field(
        None, title="목표 글자 수", description="생성할 콘텐츠의 목표 길이"
    )


# 응답 데이터 모델
class BlogCreateData(BaseModel):
    title: str = Field(..., title="블로그 제목", description="생성된 블로그 제목")
    content: str = Field(..., title="블로그 내용", description="생성된 블로그 내용")
    tags: List[str] = Field(
        default_factory=list, title="추천 태그", description="콘텐츠에 적합한 태그 목록"
    )


# 최종 응답 모델
class ResponseBlogCreate(ResponseBase[BlogCreateData]):
    """블로그 콘텐츠 생성 API 응답"""

    pass


# ============== 블로그 배포 ==============


class RequestBlogPublish(RequestBase):
    tag: str = Field(..., title="블로그 태그", description="블로그 플랫폼 종류")
    blog_id: str = Field(..., description="블로그 아이디")
    blog_pw: str = Field(..., description="블로그 비밀번호")
    blog_name: Optional[str] = Field(None, description="블로그 이름")
    post_title: str = Field(..., description="포스팅 제목")
    post_content: str = Field(..., description="포스팅 내용")
    post_tags: List[str] = Field(default_factory=list, description="포스팅 태그 목록")


# 응답 데이터 모델
class BlogPublishData(BaseModel):
    tag: str = Field(..., title="블로그 태그", description="블로그 플랫폼 종류")
    post_title: str = Field(..., title="포스팅 제목", description="배포된 포스팅 제목")
    post_url: Optional[str] = Field(
        None, title="포스팅 URL", description="배포된 포스팅 URL"
    )
    published_at: Optional[str] = Field(
        None, title="배포 시간", description="포스팅 배포 완료 시간"
    )
    publish_success: bool = Field(..., title="배포 성공 여부")

    # 디버깅 용 (Optional로 변경)
    metadata: Optional[Dict[str, Any]] = Field(
        None, description="포스팅 관련 메타데이터"
    )


# 최종 응답 모델
class ResponseBlogPublish(ResponseBase[BlogPublishData]):
    """블로그 배포 API 응답"""

    pass
