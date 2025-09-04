# app/test/test_sadagu_crawl.py
import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.errors.CustomException import InvalidItemDataException, ItemNotFoundException

client = TestClient(app)


def test_crawl_success():
    body = {
        "job_id": "test-job-001",
        "schedule_id": "schedule-001",
        "schedule_his_id": 1,
        "tag": "detail",
        "product_url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=886788894790",
        "use_selenium": False,
        "include_images": False
    }

    response = client.post("/product/crawl", json=body)
    print(response.json())
    assert response.status_code == 200
    data = response.json()
    assert data["job_id"] == body["job_id"]
    assert data["schedule_id"] == body["schedule_id"]
    assert data["product_url"] == body["product_url"]
    assert "product_detail" in data


def test_crawl_invalid_url():
    """잘못된 URL이지만 페이지는 존재하는 경우 - 빈 데이터로 성공"""
    body = {
        "job_id": "test-job-002",
        "schedule_id": "schedule-002",
        "schedule_his_id": 2,
        "tag": "detail",
        "product_url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=invalid",
        "use_selenium": False,
        "include_images": False
    }

    response = client.post("/product/crawl", json=body)
    print(response.json())

    # 200으로 성공하지만 유효한 데이터가 없는 경우를 테스트
    assert response.status_code == 200
    data = response.json()

    # 빈 데이터 또는 기본값들을 확인
    product_detail = data.get("product_detail", {})
    assert product_detail.get("title") in ["제목 없음", "제목 추출 실패"]
    assert product_detail.get("price") == 0
    assert len(product_detail.get("options", [])) == 0


def test_crawl_completely_invalid_url():
    """완전히 존재하지 않는 도메인 - 실제 오류 발생"""
    body = {
        "job_id": "test-job-002-invalid",
        "schedule_id": "schedule-002-invalid",
        "schedule_his_id": 2,
        "tag": "detail",
        "product_url": "https://nonexistent-domain-12345.com/invalid",
        "use_selenium": False,
        "include_images": False
    }

    response = client.post("/product/crawl", json=body)
    print(response.json())

    # 이 경우에는 실제로 오류가 발생해야 함
    assert response.status_code in (400, 422, 500)


def test_crawl_include_images():
    body = {
        "job_id": "test-job-003",
        "schedule_id": "schedule-003",
        "schedule_his_id": 3,
        "tag": "detail",
        "product_url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=886788894790",
        "use_selenium": False,
        "include_images": True
    }

    response = client.post("/product/crawl", json=body)
    print(response.json())
    assert response.status_code == 200
    data = response.json()
    assert data["include_images"] is True
    assert isinstance(data["product_detail"].get("product_images"), list)