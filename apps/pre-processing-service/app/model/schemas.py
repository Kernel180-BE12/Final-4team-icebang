from datetime import datetime
from typing import Optional, List, Dict, Union
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
    keyword: str

class ResponseSadaguSearch(ResponseBase):
    keyword: str
    search_results: List[Dict]

# 3단계: 매칭
class RequestSadaguMatch(RequestBase):
    keyword: str
    search_results: List[Dict]

class ResponseSadaguMatch(ResponseBase):
    keyword: str
    matched_products: List[Dict]

# 4단계: 유사도
class RequestSadaguSimilarity(RequestBase):
    keyword: str
    matched_products: List[Dict]
    search_results: Optional[List[Dict]] = None  # 3단계에서 매칭 실패시 폴백용

class ResponseSadaguSimilarity(ResponseBase):
    keyword: str
    selected_product: Optional[Dict] = None
    reason: Optional[str] = None

# 사다구몰 크롤링
class RequestSadaguCrawl(BaseModel):
    job_id: int = Field(..., description="작업 ID")
    schedule_id: int = Field(..., description="스케줄 ID")
    schedule_his_id: int = Field(..., description="스케줄 히스토리 ID")
    tag: str = Field(..., description="크롤링 태그 (예: 'detail')")
    product_url: HttpUrl = Field(..., description="크롤링할 상품의 URL")
    use_selenium: bool = Field(default=True, description="Selenium 사용 여부")
    include_images: bool = Field(default=False, description="이미지 정보 포함 여부")

class ResponseSadaguCrawl(BaseModel):
    job_id: int
    schedule_id: int
    schedule_his_id: int
    tag: str
    product_url: str
    use_selenium: bool
    include_images: bool
    product_detail: Optional[Dict] = None
    status: str
    crawled_at: Optional[str] = None

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
    pass