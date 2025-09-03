from ..model.schemas import RequestSadaguSimilarity

def select_product_by_similarity(request: RequestSadaguSimilarity) -> dict:
    """
    BERT 기반 유사도 분석 후 상품 선택
    """
    keyword = request.keyword
    candidates = request.matched_products

    # 유사도 분석 로직 적용 해야함
    selected = candidates[0] if candidates else None

    return {
        "job_id": request.job_id,
        "schedule_id": request.schedule_id,
        "keyword": keyword,
        "selected_product": selected,
        "reason": "샘플 로직: 첫 번째 매칭 선택",
        "status": "success"
    }
