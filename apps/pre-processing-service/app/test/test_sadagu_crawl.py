# app/test/test_sadagu_crawl.py
import pytest
from fastapi.testclient import TestClient
from app.main import app  # FastAPI app import
from app.errors.CustomException import InvalidItemDataException, ItemNotFoundException

client = TestClient(app)

# -------------------------------
# 성공 케이스
# -------------------------------
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

    response = client.post("/crawl", json=body)
    print(response.json())
    assert response.status_code == 200
    data = response.json()
    assert data["job_id"] == body["job_id"]
    assert data["schedule_id"] == body["schedule_id"]
    assert data["product_url"] == body["product_url"]
    assert "product_detail" in data

# -------------------------------
# 유효하지 않은 URL
# -------------------------------
def test_crawl_invalid_url():
    body = {
        "job_id": "test-job-002",
        "schedule_id": "schedule-002",
        "schedule_his_id": 2,
        "tag": "detail",
        "product_url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=invalid",
        "use_selenium": False,
        "include_images": False
    }

    response = client.post("/crawl", json=body)
    print(response.json())
    # InvalidItemDataException 발생 시 422 (또는 설정에 따라 400)
    assert response.status_code in (400, 422)

# -------------------------------
# 이미지 포함 케이스
# -------------------------------
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

    response = client.post("/crawl", json=body)
    print(response.json())
    assert response.status_code == 200
    data = response.json()
    assert data["include_images"] is True
    assert isinstance(data["product_detail"].get("product_images"), list)
