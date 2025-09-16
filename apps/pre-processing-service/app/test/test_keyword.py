import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.utils.response import Response
client = TestClient(app)


@pytest.mark.parametrize(
    "tag, category, start_date, end_date",
    [
        ("naver", "50000000", "2025-09-01", "2025-09-02"),
        ("naver", "50000001", "2025-09-01", "2025-09-02"),
        ("naver", "50000002", "2025-09-01", "2025-09-02"),
        ("naver_store", "", "2025-09-01", "2025-09-02"),
    ],
)
def test_search(tag, category, start_date, end_date):
    body = {
        "tag": tag,
        "category": category,
        "start_date": start_date,
        "end_date": end_date,
    }

    response = client.post("/keywords/search", json=body)
    assert response.status_code == 200

    response_data = response.json()
    assert response_data["success"] == True
    assert response_data["status"] == "OK"
    assert "keyword" in response_data["data"]
    assert isinstance(response_data["data"]["total_keyword"], dict)

