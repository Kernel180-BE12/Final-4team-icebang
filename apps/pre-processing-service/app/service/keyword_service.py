# Pydantic 모델을 가져오기 위해 schemas 파일 import
from ..model.schemas import RequestNaverSearch

def keyword_search(request: RequestNaverSearch) -> dict:
    """
    네이버 검색 요청을 처리하는 비즈니스 로직입니다.
    입력받은 데이터를 기반으로 응답 데이터를 생성하여 딕셔너리로 반환합니다.
    """

    response_data = request.model_dump()

    response_data["keyword"] = "밥밥밥"
    total_keyword = {1: "바밥밥", 2: "밥밥밥", 3: "바밤바"}
    response_data["total_keyword"] = total_keyword

    return response_data