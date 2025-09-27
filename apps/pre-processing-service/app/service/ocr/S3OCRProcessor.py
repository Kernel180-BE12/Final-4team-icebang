from datetime import datetime
import re
from typing import List, Dict, Any
from loguru import logger

from app.service.ocr.OCRTranslator import OCRTranslator
from app.service.ocr.S3Service import S3Service


class S3OCRProcessor:

    def __init__(self, keyword: str):
        """S3 OCR 처리기 초기화"""
        self.keyword = keyword
        self.s3_service = S3Service(keyword)
        self.ocr_translator = OCRTranslator()

    def _preprocess_text(self, text: str) -> str:
        """
        텍스트 전처리
        - 개행 문자(\n) 제거
        - 영어 문자 제거
        """
        if not text:
            return ""

        # 개행 문자를 공백으로 변경
        text = text.replace("\n", " ")

        # 영어 문자만 제거 (알파벳만, 숫자는 유지)
        text = re.sub(r"[a-zA-Z]+", " ", text)

        # 특수문자 제거
        text = re.sub(r'[\'\/\\;:<>"|\-~•]+', " ", text)

        # 연속된 공백을 하나로 정리
        text = re.sub(r"\s+", " ", text)

        # 앞뒤 공백 제거
        return text.strip()

    def process_images(self) -> Dict[str, Any]:
        """
        S3에서 이미지들을 가져와서 OCR 처리 후 지정된 형식으로 반환

        Returns:
            Dict: {
                "keyword": "키워드",
                "extraction_language": "전처리된 원본 언어 텍스트",
                "translation_language": "전처리된 번역된 한국어 텍스트"
            }
        """
        try:
            logger.info(f"키워드 '{self.keyword}' OCR 처리 시작")

            # S3에서 객체 목록 가져오기
            all_objects = self.s3_service.get_folder_objects()

            if not all_objects:
                logger.warning("S3에서 객체를 찾을 수 없습니다")
                return self._create_empty_response()

            # JPG 파일만 필터링
            jpg_files = self.s3_service.get_jpg_files(all_objects)
            logger.info(f"총 {len(jpg_files)}개의 JPG 파일 발견")

            if not jpg_files:
                logger.warning("JPG 파일을 찾을 수 없습니다")
                return self._create_empty_response()

            # 모든 추출된 텍스트와 번역된 텍스트를 수집
            all_extracted_texts = []
            all_translated_texts = []

            for jpg_file in jpg_files:
                try:
                    logger.info(f"처리 중: {jpg_file}")

                    # S3에서 이미지 데이터 가져오기
                    image_data = self.s3_service.get_image_data(jpg_file)

                    # OCR 및 번역 처리
                    ocr_result = self.ocr_translator.process_image_from_bytes(
                        image_data
                    )

                    # 성공한 경우에만 텍스트 추가
                    if ocr_result["success"] and ocr_result["original_text"]:
                        all_extracted_texts.append(ocr_result["original_text"])

                    if ocr_result["success"] and ocr_result["translated_text"]:
                        all_translated_texts.append(ocr_result["translated_text"])

                except Exception as e:
                    logger.error(f"이미지 처리 실패 ({jpg_file}): {e}")
                    continue

            # 텍스트 전처리 적용
            raw_extracted = " ".join(all_extracted_texts)
            raw_translated = " ".join(all_translated_texts)

            processed_extracted = self._preprocess_text(raw_extracted)
            processed_translated = self._preprocess_text(raw_translated)

            # 최종 응답 생성
            response = {
                "keyword": self.keyword,
                "extraction_language": processed_extracted,
                "translation_language": processed_translated,
            }

            logger.info(
                f"처리 완료: {len(all_extracted_texts)}개 텍스트 추출, {len(all_translated_texts)}개 번역"
            )
            return response

        except Exception as e:
            logger.error(f"OCR 처리 과정에서 오류 발생: {e}")
            return self._create_empty_response()

    def _create_empty_response(self) -> Dict[str, Any]:
        """빈 응답 생성"""
        return {
            "keyword": self.keyword,
            "extraction_language": "",
            "translation_language": "",
        }
