from app.errors.CustomException import CustomException
from typing import List, Optional

class BlogLoginException(CustomException):
    """
    블로그 로그인 실패 예외
    @:param platform: 로그인하려는 플랫폼 (네이버, 티스토리 등)
    @:param reason: 로그인 실패 이유
    """
    def __init__(self, platform: str, reason: str = "인증 정보가 올바르지 않습니다"):
        super().__init__(
            status_code=401,
            detail=f"{platform} 로그인에 실패했습니다. {reason}",
            code="BLOG_LOGIN_FAILED"
        )

class BlogPostPublishException(CustomException):
    """
    블로그 포스트 발행 실패 예외
    @:param platform: 발행하려는 플랫폼
    @:param reason: 발행 실패 이유
    """
    def __init__(self, platform: str, reason: str = "포스트 발행 중 오류가 발생했습니다"):
        super().__init__(
            status_code=422,
            detail=f"{platform} 포스트 발행에 실패했습니다. {reason}",
            code="BLOG_POST_PUBLISH_FAILED"
        )

class BlogContentValidationException(CustomException):
    """
    블로그 콘텐츠 유효성 검사 실패 예외
    @:param field: 유효성 검사 실패한 필드
    @:param reason: 실패 이유
    """
    def __init__(self, field: str, reason: str):
        super().__init__(
            status_code=400,
            detail=f"콘텐츠 유효성 검사 실패: {field} - {reason}",
            code="BLOG_CONTENT_VALIDATION_FAILED"
        )

class BlogElementInteractionException(CustomException):
    """
    블로그 페이지 요소와의 상호작용 실패 예외
    @:param element: 상호작용하려던 요소
    @:param action: 수행하려던 액션
    """
    def __init__(self, element: str, action: str):
        super().__init__(
            status_code=422,
            detail=f"블로그 페이지 요소 상호작용 실패: {element}에서 {action} 작업 실패",
            code="BLOG_ELEMENT_INTERACTION_FAILED"
        )

class BlogServiceUnavailableException(CustomException):
    """
    블로그 서비스 이용 불가 예외
    @:param platform: 이용 불가한 플랫폼
    @:param reason: 이용 불가 이유
    """
    def __init__(self, platform: str, reason: str = "서비스가 일시적으로 이용 불가합니다"):
        super().__init__(
            status_code=503,
            detail=f"{platform} 서비스 이용 불가: {reason}",
            code="BLOG_SERVICE_UNAVAILABLE"
        )

class BlogConfigurationException(CustomException):
    """
    블로그 서비스 설정 오류 예외
    @:param config_item: 설정 오류 항목
    """
    def __init__(self, config_item: str):
        super().__init__(
            status_code=500,
            detail=f"블로그 서비스 설정 오류: {config_item}",
            code="BLOG_CONFIGURATION_ERROR"
        )