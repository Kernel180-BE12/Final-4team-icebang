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
    """
    아이템을 찾을수 없는 예외
    @:param item_id: 찾을수 없는 아이템의 ID
    """

    def __init__(self, item_id: int):
        super().__init__(
            status_code=404,
            detail=f"{item_id}를 찾을수 없습니다.",
            code="ITEM_NOT_FOUND",
        )


class InvalidItemDataException(CustomException):
    """
    데이터 유효성 검사 실패 예외
    """

    def __init__(self):
        super().__init__(
            status_code=422,
            detail="데이터가 유효하지않습니다..",
            code="INVALID_ITEM_DATA",
        )


class DatabaseConnectionException(CustomException):
    """
    데이터베이스 연결 실패 예외
    """

    def __init__(self):
        super().__init__(
            status_code=500,
            detail="데이터베이스 연결에 실패했습니다.",
            code="DATABASE_CONNECTION_ERROR",
        )
