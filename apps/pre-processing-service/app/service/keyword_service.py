# Pydantic 모델을 가져오기 위해 schemas 파일 import
import json
import random

import httpx
from starlette import status

from ..errors.CustomException import InvalidItemDataException
from ..model.schemas import RequestNaverSearch

async def keyword_search(request: RequestNaverSearch) -> dict:
    """
    네이버 검색 요청을 처리하는 비즈니스 로직입니다.
    입력받은 데이터를 기반으로 응답 데이터를 생성하여 딕셔너리로 반환합니다.
    """

    #키워드 검색
    if request.tag == "naver":
        trending_keywords  = await search_naver_rank(**request.model_dump(include={'category', 'start_date', 'end_date'}))
    elif request.tag == "naver_store":
        trending_keywords = await search_naver_store()
    else :
        raise InvalidItemDataException()

    if not trending_keywords:
        raise InvalidItemDataException()

    response_data = request.model_dump()
    response_data["keyword"] = random.choice(list(trending_keywords.values()))
    response_data["total_keyword"] = trending_keywords
    response_data["status"] = "success"
    return response_data

async def search_naver_rank(category,start_date,end_date) -> dict[int,str]:
    """
    네이버 데이터 랩 키워드 검색 모듈
    """
    url = "https://datalab.naver.com/shoppingInsight/getCategoryKeywordRank.naver"
    headers = {
        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
        "Referer": "https://datalab.naver.com/shoppingInsight/sCategory.naver",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
    }
    keywords_dic ={}
    async with httpx.AsyncClient() as client:
        for page in range(1, 3):
            payload = {
                "cid": category,
                "timeUnit": "date",
                "startDate": start_date,
                "endDate": end_date,
                "age": "",
                "gender": "",
                "device": "",
                "page": page,
            }
            try:
                response = await client.post(url, headers=headers, data=payload)
                response.raise_for_status()
                data = response.json()
                for item in data.get('ranks', []):
                    keywords_dic[item.get('rank')] = item.get('keyword')
            except (httpx.HTTPStatusError, httpx.RequestError, json.JSONDecodeError) as e:
                print(f"네이버 데이터랩에서 데이터를 가져오는 데 실패했습니다: {e}")
                raise InvalidItemDataException
        return keywords_dic


async def search_naver_store() -> dict[int,str]:
    """
    네이버 스토어의 일일 인기 검색어 순위 데이터를 가져옵니다.
    API 응답의 'keyword' 필드를 'title'로 변경하여 전체 순위 목록을 반환합니다.
    """
    url = "https://snxbest.naver.com/api/v1/snxbest/keyword/rank?ageType=ALL&categoryId=A&sortType=KEYWORD_POPULAR&periodType=DAILY"
    headers = {}

    async with httpx.AsyncClient() as client:
        try:
            # API에 GET 요청을 보냅니다.
            response = await client.get(url, headers=headers)
            response.raise_for_status()  # HTTP 오류 발생 시 예외를 일으킵니다.
            data = response.json()
            keyword_dict = {}

            for item in data:
                keyword_dict[item['rank']] = item['title']

            return keyword_dict

        except (httpx.HTTPStatusError, httpx.RequestError, json.JSONDecodeError) as e:
            print(f"네이버 스토어에서 데이터를 가져오는 데 실패했습니다: {e}")
            raise InvalidItemDataException from e