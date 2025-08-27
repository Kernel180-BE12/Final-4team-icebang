# app/errors/CustomException.py
class CustomException(Exception):
    """
    개발자가 비지니스 로직에 맞게 의도적으로 에러를 정의
    """
    def __init__(self, status_code: int, detail: str, code: str):
        self.status_code = status_code
        self.detail = detail
        self.code = code

# 구체적인 커스텀 예외 정의
class ItemNotFoundException(CustomException):
    def __init__(self, item_id: int):
        super().__init__(
            status_code=404,
            detail=f"{item_id}를 찾을수 없습니다.",
            code="ITEM_NOT_FOUND"
        )

class InvalidItemDataException(CustomException):
    def __init__(self):
        super().__init__(
            status_code=422,
            detail="데이터가 유효하지않습니다..",
            code="INVALID_ITEM_DATA"
        )