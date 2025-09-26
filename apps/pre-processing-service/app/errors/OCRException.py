from app.errors.CustomException import CustomException


class ExtractTextException(CustomException):

    def __init__(self):
        super().__init__(
            status_code=500,
            detail="이미지에서 텍스트 추출에 실패했습니다.",
            code="OCR_EXTRACTION_FAILED",
        )

class TranslationException(CustomException):

    def __init__(self):
        super().__init__(
            status_code=500,
            detail="텍스트 번역에 실패했습니다.",
            code="TRANSLATION_FAILED",
        )