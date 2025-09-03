from ..model.schemas import RequestSadaguSearch
import urllib.parse

def search_products(request: RequestSadaguSearch) -> dict:
    """
    키워드 기반으로 상품을 검색하는 비즈니스 로직
    """
    keyword = request.keyword
    encoded_keyword = urllib.parse.quote(keyword)

    # Selenium/requests 로직 추가 해야함
    search_results = [
        {"url": f"https://ssadagu.kr/view.php?id=123"},
        {"url": f"https://ssadagu.kr/view.php?id=456"}
    ]

    return {
        "job_id": request.job_id,
        "schedule_id": request.schedule_id,
        "keyword": keyword,
        "search_results": search_results,
        "status": "success"
    }
