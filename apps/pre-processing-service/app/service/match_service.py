from ..model.schemas import RequestSadaguMatch

def match_products(request: RequestSadaguMatch) -> dict:
    """
    키워드 매칭 로직 (MeCab 등 사용)
    """
    keyword = request.keyword
    products = request.search_results

    matched = [p for p in products if keyword in p["title"]]

    return {
        "job_id": request.job_id,
        "schedule_id": request.schedule_id,
        "keyword": keyword,
        "matched_products": matched,
        "status": "success"
    }