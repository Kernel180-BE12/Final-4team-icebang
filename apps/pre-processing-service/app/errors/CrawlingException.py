from CustomException import CustomException
from typing import List

class PageLoadTimeoutException(CustomException):
    """
    페이지 로드 타임아웃 예외
    @:param url: 로드하려는 페이지의 URL
    """
    def __init__(self, url : str):
        super().__init__(
            status_code=408,
            detail=f"페이지 로드가 시간 초과되었습니다. URL: {url}",
            code="PAGE_LOAD_TIMEOUT"
        )

class WebDriverConnectionException(CustomException):
    """
    웹 드라이버 연결 실패 예외
    """
    def __init__(self):
        super().__init__(
            status_code=500,
            detail="웹 드라이버 연결에 실패했습니다.",
            code="WEBDRIVER_ERROR"
        )


class ElementNotFoundException(CustomException):
    """
    특정 HTML 요소를 찾을 수 없는 예외
    @:param selector: 찾으려는 요소의 CSS 선택자
    """
    def __init__(self, selector: str):
        super().__init__(
            status_code=404,
            detail=f"요소를 찾을 수 없습니다. 선택자: {selector}",
            code="ELEMENT_NOT_FOUND"
        )

class HtmlParsingException(CustomException):
    """
    HTML 파싱 실패 예외
    @:param reason: 파싱 실패 이유
    """
    def __init__(self, reason: str):
        super().__init__(
            status_code=422,
            detail=f"HTML 파싱에 실패했습니다. 이유: {reason}",
            code="HTML_PARSING_ERROR"
        )

class DataExtractionException(CustomException):
    """
    데이터 추출 실패 예외
    @:param field: 추출하려는 데이터 필드 목록
    """
    def __init__(self, field: List[str]):
        super().__init__(
            status_code=422,
            detail=f"데이터 추출에 실패했습니다. 필드: {', '.join(field)}",
            code="DATA_EXTRACTION_ERROR"
        )

