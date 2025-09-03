from datetime import datetime
from typing import Optional
from pydantic import BaseModel



#기본 요청
class RequestBase(BaseModel):
    job_id: str
    schedule_id: str
    schedule_his_id: Optional[int] = None

#기본 응답
class ResponseBase(BaseModel):
    job_id: str
    schedule_id: str
    status: str


#네이버 키워드 추출
class RequestNaverSearch(RequestBase):
    tag: str
    category: str
    startDate :datetime
    endDate :datetime

class ResponseNaverSearch(ResponseBase):
    category: str
    keyword: str
    total_keyword: dict[int, str]


# #키워드 사다구몰 검증
# class RequestSadaguValidate(RequestBase):
#     tag: str
#     category: str
#
# class ResponsetSadaguValidate(ResponseBase):
#     keyword: str

# 2단계: 검색
class RequestSadaguSearch(RequestBase):
    keyword: str

class ResponseSadaguSearch(ResponseBase):
    keyword: str
    search_results: list[dict]

#사다구몰 상품 크롤링
class RequestSadaguCrawl(RequestBase):
    tag: str
    category: str

class ResponsetSadaguCrawl(ResponseBase):
    pass

#블로그 생성
class RequestBlogCreate(RequestBase):
    tag: str
    category: str

class ResponsetBlogCreate(ResponseBase):
    pass

#블로그 배포
class RequestBlogPublish(RequestBase):
    tag: str
    category: str

class ResponsetBlogPublish(ResponseBase):
    pass
