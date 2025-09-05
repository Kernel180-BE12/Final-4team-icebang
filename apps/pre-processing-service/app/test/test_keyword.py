import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

JOB_ID = 1
SCHEDULE_ID = 1
SCHEDULE_HIS_ID = 1


def test_read_root():
    response = client.get("/keyword/")
    assert response.status_code == 200
    assert response.json() == {"message": "keyword API"}


@pytest.mark.parametrize("tag, category, start_date, end_date", [
    ("naver", "50000000", "2025-09-01", "2025-09-02"),
    ("naver", "50000001", "2025-09-01", "2025-09-02"),
    ("naver", "50000002", "2025-09-01", "2025-09-02"),
    ("naver_store", "", "2025-09-01", "2025-09-02"),
])
def test_search(tag, category, start_date, end_date):
    body = {
        "job_id": JOB_ID,
        "schedule_id": SCHEDULE_ID,
        "schedule_his_id": SCHEDULE_HIS_ID,  # 오타 수정
        "tag": tag,
        "category": category,
        "start_date": start_date,
        "end_date": end_date
    }

    response = client.post("/keyword/search", json=body)
    assert response.status_code == 200

    response_data = response.json()
    assert response_data["job_id"] == body["job_id"]
    assert response_data["schedule_id"] == body["schedule_id"]
    assert response_data["schedule_his_id"] == body["schedule_his_id"]  # 오타 수정
    assert response_data["status"] == "success"
    assert "keyword" in response_data
    assert isinstance(response_data["total_keyword"], dict)