import pytest
from fastapi.testclient import TestClient
from ..main import app  # main.py에서 FastAPI app 객체를 가져옵니다.

client = TestClient(app)

JOB_ID = 1
SCHEDULE_ID = 1
SCHEDULE_HIS_ID = 1



def test_read_root():
    # client를 사용하여 API에 요청을 보냅니다.
    response = client.get("/keyword/")
    # HTTP 상태 코드가 200 OK인지 확인합니다.
    assert response.status_code == 200
    # 응답 본문(JSON)이 예상과 같은지 확인합니다.
    assert response.json() == {"message": "keyword API"}


@pytest.mark.parametrize("tag, category, start_date, end_date", [
    ("naver","50000000","2025-09-01","2025-09-02"),
    ("naver","50000001","2025-09-01","2025-09-02"),
    ("naver","50000002","2025-09-01","2025-09-02"),
    # ("naver","50000002","2025-08-08","2025-08-09"),
    ("naver_store","","2025-09-01","2025-09-02"),
])
def test_search(tag,category, start_date, end_date):

    body = {
        "job_id":JOB_ID,
        "schedule_id": SCHEDULE_ID,
        "sschdule_his_id":SCHEDULE_HIS_ID,
        "tag":tag,
        "category":category,
        "start_date":start_date,
        "end_date":end_date
    }
    response = client.post("/keyword/search",json=body)
    assert response.json()["job_id"] == body["job_id"]
    assert response.json()["schedule_id"] == body["schedule_id"]
    assert response.json()["sschdule_his_id"] == body["sschdule_his_id"]
    assert response.json()["status"] == "success"
    assert "keyword" in response.json()
    assert isinstance(response.json()["total_keyword"], dict)
    assert response.status_code == 200