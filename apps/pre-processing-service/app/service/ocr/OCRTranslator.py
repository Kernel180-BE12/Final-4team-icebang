import os
import json
from google.cloud import vision
from google.oauth2 import service_account
from deep_translator import GoogleTranslator
from loguru import logger
import io


class OCRTranslator:

    def __init__(self):
        """다국어 OCR 번역기 초기화 (중국어, 일본어, 영어 -> 한국어)"""

        self.source_languages = ["zh-CN", "ja", "en"]
        self.target_language = "ko"

        # 각 언어별 번역기 초기화
        self.translators = {}
        for lang in self.source_languages:
            try:
                self.translators[lang] = GoogleTranslator(
                    source=lang, target=self.target_language
                )
                logger.info(f"{lang} -> {self.target_language} 번역기 초기화 완료")
            except Exception as e:
                logger.error(f"{lang} 번역기 초기화 실패: {e}")

        # Google Vision API 클라이언트 초기화
        self.vision_client = self._initialize_vision_client()

    def _initialize_vision_client(self):
        """Google Vision API 클라이언트 초기화"""
        try:
            # # 환경변수에서 JSON 문자열로 인증정보 가져오기
            # creds_json = os.getenv('GOOGLE_APPLICATION_CREDENTIALS_JSON')
            # if creds_json:
            #     logger.info("환경변수에서 Google 인증정보 로드")
            #     creds_dict = json.loads(creds_json)
            #     if "private_key" in creds_dict:
            #         while "\\n" in creds_dict["private_key"]:
            #             creds_dict["private_key"] = creds_dict["private_key"].replace("\\n", "\n")
            #     credentials = service_account.Credentials.from_service_account_info(creds_dict)
            #     return vision.ImageAnnotatorClient(credentials=credentials)

            # 파일 경로에서 인증정보 가져오기
            creds_file = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
            if creds_file and os.path.exists(creds_file):
                logger.info(f"파일에서 Google 인증정보 로드: {creds_file}")
                return vision.ImageAnnotatorClient()

            # 기본 인증 (Cloud Shell, GCE 등)
            logger.info("기본 인증으로 Google Vision 클라이언트 초기화")
            return vision.ImageAnnotatorClient()

        except Exception as e:
            logger.error(f"Google Vision 클라이언트 초기화 실패: {e}")
            raise

    def _detect_language(self, text):
        """텍스트에서 언어 감지"""
        if not text:
            return None

        # 각 언어별 특징 문자 카운트
        chinese_chars = sum(1 for char in text if "\u4e00" <= char <= "\u9fff")
        hiragana_chars = sum(1 for char in text if "\u3040" <= char <= "\u309f")
        katakana_chars = sum(1 for char in text if "\u30a0" <= char <= "\u30ff")
        english_chars = sum(1 for char in text if char.isascii() and char.isalpha())

        total_chars = len(
            [
                char
                for char in text
                if char.isalpha()
                or "\u4e00" <= char <= "\u9fff"
                or "\u3040" <= char <= "\u30ff"
            ]
        )

        if total_chars == 0:
            return None

        # 일본어 특징 문자(히라가나/가타카나)가 있으면 일본어로 판단
        if (hiragana_chars + katakana_chars) > 0:
            return "ja"

        # 영어 문자가 대부분이면 영어로 판단
        if english_chars / total_chars > 0.7:
            return "en"

        # 중국어 문자가 있으면 중국어로 판단
        if chinese_chars > 0:
            return "zh-CN"

        # 기본값은 중국어로 설정
        return "zh_CN"

    def _extract_text_from_content(self, image_content: bytes):
        """Google Vision API를 사용해 이미지에서 텍스트 추출"""
        try:
            # Vision API Image 객체 생성
            image = vision.Image(content=image_content)

            # 텍스트 감지 요청
            response = self.vision_client.text_detection(image=image)

            if response.error.message:
                raise Exception(f"Vision API Error: {response.error.message}")

            # 텍스트 추출
            texts = response.text_annotations
            if texts:
                # 첫 번째 요소가 전체 텍스트
                extracted_text = texts[0].description
                logger.info(f"Vision API로 추출된 텍스트: {extracted_text}")
                return extracted_text.strip()
            else:
                logger.warning("이미지에서 텍스트를 찾을 수 없습니다")
                return ""

        except Exception as e:
            logger.error(f"Vision API 텍스트 추출 오류: {e}")
            raise

    def _extract_text_from_path(self, image_path):
        """이미지에서 텍스트 추출 (파일 경로)"""
        try:
            # 이미지 파일 읽기
            with io.open(image_path, "rb") as image_file:
                content = image_file.read()

            return self._extract_text_from_content(content)

        except Exception as e:
            logger.error(f"Error during OCR: {e}")
            raise

    def _extract_text_from_bytes(self, image_data: bytes):
        """이미지에서 텍스트 추출 (바이트 데이터)"""
        try:
            return self._extract_text_from_content(image_data)
        except Exception as e:
            logger.error(f"Error during OCR from bytes: {e}")
            raise

    def translate_text(self, text, source_lang=None):
        """텍스트를 한국어로 번역"""
        if not text:
            return "", None

        # 언어가 지정되지 않은 경우 자동 감지
        if source_lang is None:
            source_lang = self._detect_language(text)

        if source_lang is None or source_lang not in self.translators:
            logger.warning(f"지원하지 않는 언어이거나 감지할 수 없음: {source_lang}")
            return "", source_lang

        try:
            result = self.translators[source_lang].translate(text)
            return result if result is not None else "", source_lang
        except Exception as e:
            logger.error(f"번역 오류 ({source_lang}): {e}")
            return "", source_lang

    def process_image(self, image_path, source_lang=None):
        """이미지에서 텍스트 추출 후 한국어로 번역 (파일 경로)"""
        try:
            # 텍스트 추출
            extracted_text = self._extract_text_from_path(image_path)
            logger.info(f"추출된 텍스트: {extracted_text}")

            # 번역
            translated_text, detected_lang = self.translate_text(
                extracted_text, source_lang
            )
            logger.info(f"번역된 텍스트 ({detected_lang}): {translated_text}")

            return {
                "original_text": extracted_text,
                "translated_text": translated_text,
                "detected_language": detected_lang,
                "success": True,
                "error": None,
            }

        except Exception as e:
            logger.error(f"이미지 처리 오류: {e}")
            return {
                "original_text": "",
                "translated_text": "",
                "detected_language": None,
                "success": False,
                "error": str(e),
            }

    def process_image_from_bytes(self, image_data: bytes, source_lang=None):
        """이미지에서 텍스트 추출 후 한국어로 번역 (바이트 데이터)"""
        try:
            # 텍스트 추출
            extracted_text = self._extract_text_from_bytes(image_data)
            logger.info(f"추출된 텍스트: {extracted_text}")

            # 번역
            translated_text, detected_lang = self.translate_text(
                extracted_text, source_lang
            )
            logger.info(f"번역된 텍스트 ({detected_lang}): {translated_text}")

            return {
                "original_text": extracted_text,
                "translated_text": translated_text,
                "detected_language": detected_lang,
                "success": True,
                "error": None,
            }

        except Exception as e:
            logger.error(f"이미지 처리 오류: {e}")
            return {
                "original_text": "",
                "translated_text": "",
                "detected_language": None,
                "success": False,
                "error": str(e),
            }

    def process_chinese_only(self, image_data):
        """중국어 전용 처리"""
        result = self.process_image_from_bytes(image_data, source_lang="zh-CN")
        return {
            "chinese_text": result["original_text"],
            "korean_text": result["translated_text"],
            "success": result["success"],
        }

    def process_japanese_only(self, image_data):
        """일본어 전용 처리"""
        result = self.process_image_from_bytes(image_data, source_lang="ja")
        return {
            "japanese_text": result["original_text"],
            "korean_text": result["translated_text"],
            "success": result["success"],
        }

    def process_english_only(self, image_data):
        """영어 전용 처리"""
        result = self.process_image_from_bytes(image_data, source_lang="en")
        return {
            "english_text": result["original_text"],
            "korean_text": result["translated_text"],
            "success": result["success"],
        }
