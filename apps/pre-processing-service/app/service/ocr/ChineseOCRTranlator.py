import pytesseract
from PIL import Image
from deep_translator import GoogleTranslator
from loguru import logger
import io

from app.errors.OCRException import ExtractTextException, TranslationException
from app.service.ocr.S3Service import S3Service


class ChineseOCRTranslator:

    def __init__(self):
        self.translator = GoogleTranslator(source='zh-CN', target='ko')

    def _extract_chinese_text(self, image_path):
        """이미지에서 중국어 텍스트 추출 """

        try:
            image = Image.open(image_path)
            text = pytesseract.image_to_string(
                image,
                lang='chi_sim+chi_tra',
                config='--psm 6'
            )
            return text.strip()
        except Exception as e:
            logger.error(f"Error during OCR: {e}")
            raise ExtractTextException()

    def _extract_chinese_text_from_bytes(self, image_data: bytes):
        """이미지에서 중국어 텍스트 추출 (바이트 데이터)"""
        try:
            image = Image.open(io.BytesIO(image_data))
            text = pytesseract.image_to_string(
                image,
                lang='chi_sim+chi_tra',
                config='--psm 6'
            )
            return text.strip()
        except Exception as e:
            logger.error(f"Error during OCR from bytes: {e}")
            raise ExtractTextException()

    def translate_to_korean(self, text):
        """중국어 텍스트를 한국어로 번역"""

        if not text:
            return ""

        try:
            result = self.translator.translate(text)
            return result

        except Exception as e:
            logger.error(f"Error during translation: {e}")
            raise TranslationException()

    def process_image(self, image_path):
        """이미지에서 중국어 텍스트 추출 후 한국어로 번역"""

        chinese_text = self._extract_chinese_text(image_path)
        logger.info("추출된 중국어 텍스트: " + chinese_text)

        korean_text = self.translate_to_korean(chinese_text)
        logger.info("번역된 한국어 텍스트: " + korean_text)

        return {
            "chinese_text": chinese_text,
            "korean_text": korean_text,
            "success": True
        }

    def process_image_from_bytes(self, image_data: bytes):
        """이미지에서 중국어 텍스트 추출 후 한국어로 번역 (바이트 데이터)"""
        chinese_text = self._extract_chinese_text_from_bytes(image_data)
        logger.info("추출된 중국어 텍스트: " + chinese_text)

        korean_text = self.translate_to_korean(chinese_text)
        logger.info("번역된 한국어 텍스트: " + korean_text)

        return {
            "chinese_text": chinese_text,
            "korean_text": korean_text,
            "success": True
        }

# if __name__ == "__main__":
#     s3service = S3Service("가디건")
#     ocr = ChineseOCRTranslator()
#
#     lists = s3service.get_folder_objects()
#     print(lists)
#     jpg_files = s3service.get_jpg_files(lists)
#     print(jpg_files)
#
#     results = []
#     for file in jpg_files:
#
#         image_data = s3service.get_image_data(file)
#
#         result = ocr.process_image_from_bytes(image_data)
#         result["s3_key"] = file
#         results.append(result)
#
#     print(results)




