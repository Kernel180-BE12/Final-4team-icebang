import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_search_success():
    """상품 검색 성공 테스트"""
    body = {"keyword": "반지"}

    response = client.post("/products/search", json=body)
    print(f"Search Response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    assert data["success"] == True
    assert data["status"] == "OK"
    assert data["data"]["keyword"] == body["keyword"]
    assert isinstance(data["data"]["search_results"], list)


def test_search_empty_keyword():
    """빈 키워드 검색 테스트"""
    body = {"keyword": ""}

    response = client.post("/products/search", json=body)
    print(f"Empty keyword response: {response.json()}")

    # 빈 키워드라도 에러가 아닌 빈 결과를 반환해야 함
    assert response.status_code == 200
    data = response.json()
    assert data["success"] == True
    assert data["status"] == "OK"
    assert data["data"]["search_results"] == []


def test_search_nonexistent_keyword():
    """존재하지 않는 키워드 검색"""
    body = {
        "keyword": "zxcvbnmasdfghjklqwertyuiop123456789",
    }

    response = client.post("/products/search", json=body)
    print(f"Nonexistent keyword response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    # 검색 결과가 없어도 성공으로 처리
    assert data["success"] == True
    assert data["status"] == "OK"
    assert isinstance(data["data"]["search_results"], list)
