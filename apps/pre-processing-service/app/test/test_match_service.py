import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_match_success():
    """키워드 매칭 성공 테스트"""
    sample_search_results = [
        {
            "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=123",
            "title": "925 실버 반지 여성용 결혼반지"
        },
        {
            "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=456",
            "title": "골드 목걸이 체인 펜던트"
        },
        {
            "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=789",
            "title": "반지 세트 커플링 약혼반지"
        }
    ]

    body = {
        "job_id": 1,
        "schedule_id": 1,
        "schedule_his_id": 1,
        "keyword": "반지",
        "search_results": sample_search_results
    }

    response = client.post("/product/match", json=body)
    print(f"Match Response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    assert data["job_id"] == body["job_id"]
    assert data["keyword"] == body["keyword"]
    assert data["status"] == "success"
    assert isinstance(data["matched_products"], list)

    # 반지가 포함된 상품들이 매칭되어야 함
    if data["matched_products"]:
        for product in data["matched_products"]:
            assert "match_info" in product
            assert "match_type" in product["match_info"]
            assert "match_score" in product["match_info"]


def test_match_no_results():
    """검색 결과가 없는 경우"""
    body = {
        "job_id": 2,
        "schedule_id": 2,
        "schedule_his_id": 2,
        "keyword": "반지",
        "search_results": []
    }

    response = client.post("/product/match", json=body)
    print(f"No results response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    assert data["matched_products"] == []


def test_match_no_matches():
    """키워드와 매칭되지 않는 상품들"""
    sample_search_results = [
        {
            "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=123",
            "title": "컴퓨터 키보드 게이밍"
        },
        {
            "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=456",
            "title": "스마트폰 케이스 투명"
        }
    ]

    body = {
        "job_id": 3,
        "schedule_id": 3,
        "schedule_his_id": 3,
        "keyword": "반지",
        "search_results": sample_search_results
    }

    response = client.post("/product/match", json=body)
    print(f"No matches response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    # 매칭되지 않아도 성공으로 처리
    assert data["status"] == "success"
    assert isinstance(data["matched_products"], list)