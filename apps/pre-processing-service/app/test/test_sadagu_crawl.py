import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_crawl_success():
    body = {
        "job_id": 1,  # 문자열 -> 숫자로 수정
        "schedule_id": 1,  # 문자열 -> 숫자로 수정
        "schedule_his_id": 1,
        "tag": "detail",
        "product_url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=886788894790",
        "use_selenium": False,
        "include_images": False
    }

    response = client.post("/product/crawl", json=body)
    print(f"Response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    assert data["job_id"] == body["job_id"]
    assert data["schedule_id"] == body["schedule_id"]
    assert data["product_url"] == body["product_url"]
    assert "product_detail" in data


def test_crawl_invalid_url():
    """잘못된 URL이지만 페이지는 존재하는 경우"""
    body = {
        "job_id": 2,
        "schedule_id": 2,
        "schedule_his_id": 2,
        "tag": "detail",
        "product_url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=invalid",
        "use_selenium": False,
        "include_images": False
    }

    response = client.post("/product/crawl", json=body)
    print(f"Response: {response.json()}")

    assert response.status_code == 200
    data = response.json()

    product_detail = data.get("product_detail", {})
    assert product_detail.get("title") in ["제목 없음", "제목 추출 실패", None]
    assert product_detail.get("price", 0) == 0


def test_crawl_completely_invalid_url():
    """완전히 존재하지 않는 도메인"""
    body = {
        "job_id": 3,
        "schedule_id": 3,
        "schedule_his_id": 3,
        "tag": "detail",
        "product_url": "https://nonexistent-domain-12345.com/invalid",
        "use_selenium": False,
        "include_images": False
    }

    response = client.post("/product/crawl", json=body)
    print(f"Response: {response.json()}")

    assert response.status_code in (400, 422, 500)


def test_crawl_include_images():
    body = {
        "job_id": 4,
        "schedule_id": 4,
        "schedule_his_id": 4,
        "tag": "detail",
        "product_url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=886788894790",
        "use_selenium": False,
        "include_images": True
    }

    response = client.post("/product/crawl", json=body)
    print(f"Response: {response.json()}")

    assert response.status_code == 200
    data = response.json()
    assert data["include_images"] is True
    assert isinstance(data["product_detail"].get("product_images"), list)