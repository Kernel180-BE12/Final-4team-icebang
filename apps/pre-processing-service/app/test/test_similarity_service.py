import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_similarity_with_matched_products():
    """매칭된 상품들 중에서 유사도 분석"""
    matched_products = [
        {
            "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=123",
            "title": "925 실버 반지 여성용",
            "match_info": {
                "match_type": "exact",
                "match_score": 1.0,
                "match_reason": "완전 매칭",
            },
        },
        {
            "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=456",
            "title": "반지 세트 커플링",
            "match_info": {
                "match_type": "morphological",
                "match_score": 0.8,
                "match_reason": "형태소 매칭",
            },
        },
    ]

    body = {
        "keyword": "반지",
        "matched_products": matched_products,
    }

    response = client.post("/products/similarity", json=body)
    print(f"Similarity Response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    assert data["keyword"] == body["keyword"]
    assert data["status"] == "success"

    if data["selected_product"]:
        assert "similarity_info" in data["selected_product"]
        assert "similarity_score" in data["selected_product"]["similarity_info"]
        assert data["reason"] is not None


def test_similarity_fallback_to_search_results():
    """매칭 실패시 전체 검색 결과에서 유사도 분석"""
    search_results = [
        {
            "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=123",
            "title": "실버 링 악세서리",
        },
        {
            "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=456",
            "title": "골드 반지 여성",
        },
    ]

    body = {
        "keyword": "반지",
        "matched_products": [],  # 매칭된 상품 없음
        "search_results": search_results,  # 폴백용
    }

    response = client.post("/products/similarity", json=body)
    print(f"Fallback Response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "success"

    # 폴백 모드에서는 임계값을 통과한 경우에만 상품이 선택됨
    if data["selected_product"]:
        assert "similarity_info" in data["selected_product"]
        assert (
            data["selected_product"]["similarity_info"]["analysis_mode"]
            == "fallback_similarity_only"
        )


def test_similarity_single_candidate():
    """후보가 1개만 있는 경우"""
    single_product = [
        {
            "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=123",
            "title": "925 실버 반지 여성용",
            "match_info": {"match_type": "exact", "match_score": 1.0},
        }
    ]

    body = {
        "keyword": "반지",
        "matched_products": single_product,
    }

    response = client.post("/products/similarity", json=body)
    print(f"Single candidate response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    assert data["selected_product"] is not None
    assert (
        data["selected_product"]["similarity_info"]["analysis_type"]
        == "single_candidate"
    )


def test_similarity_no_candidates():
    """후보가 없는 경우"""
    body = {
        "keyword": "반지",
        "matched_products": [],
        "search_results": [],
    }

    response = client.post("/products/similarity", json=body)
    print(f"No candidates response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    assert data["selected_product"] is None
    assert "검색 결과가 모두 없음" in data["reason"]
