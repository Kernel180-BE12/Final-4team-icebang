class Response:
    @staticmethod
    def ok(data: dict, message: str = "OK") -> dict:
        """성공 응답"""
        return {"success": True, "data": data, "status": "OK", "message": message}

    @staticmethod
    def error(message: str = "오류가 발생했습니다", data: dict = None) -> dict:
        """에러 응답"""
        return {
            "success": False,
            "data": data or {},
            "status": "ERROR",
            "message": message,
        }

    @staticmethod
    def not_found(message: str = "결과를 찾을 수 없습니다", data: dict = None) -> dict:
        """검색 결과 없음"""
        return {
            "success": True,  # 에러가 아닌 정상 처리
            "data": data or {},
            "status": "NOT_FOUND",
            "message": message,
        }
