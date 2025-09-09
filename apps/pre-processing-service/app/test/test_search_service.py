import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_search_success():
    """상품 검색 성공 테스트"""
    body = {"job_id": 1, "schedule_id": 1, "schedule_his_id": 1, "keyword": "반지"}

    response = client.post("/product/search", json=body)
    print(f"Search Response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    assert data["job_id"] == body["job_id"]
    assert data["keyword"] == body["keyword"]
    assert data["status"] == "success"
    assert isinstance(data["search_results"], list)


def test_search_empty_keyword():
    """빈 키워드 검색 테스트"""
    body = {"job_id": 2, "schedule_id": 2, "schedule_his_id": 2, "keyword": ""}

    response = client.post("/product/search", json=body)
    print(f"Empty keyword response: {response.json()}")

    # 빈 키워드라도 에러가 아닌 빈 결과를 반환해야 함
    assert response.status_code == 200
    data = response.json()
    assert data["search_results"] == []


def test_search_nonexistent_keyword():
    """존재하지 않는 키워드 검색"""
    body = {
        "job_id": 3,
        "schedule_id": 3,
        "schedule_his_id": 3,
        "keyword": "zxcvbnmasdfghjklqwertyuiop123456789",
    }

    response = client.post("/product/search", json=body)
    print(f"Nonexistent keyword response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    # 검색 결과가 없어도 성공으로 처리
    assert data["status"] == "success"
    assert isinstance(data["search_results"], list)
