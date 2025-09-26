import os
import easyocr
from deep_translator import GoogleTranslator
from loguru import logger
import numpy as np
from PIL import Image
import io


class ChineseOCRTranslator:

    def __init__(self):
        self.translator = GoogleTranslator(source='zh-CN', target='ko')

        # EasyOCR 리더 초기화 (중국어 간체, 중국어 번체, 영어 지원)
        self.ocr_reader = self._initialize_ocr_reader()

    def _initialize_ocr_reader(self):
        """EasyOCR 리더 초기화"""
        try:
            logger.info("EasyOCR 리더 초기화 중...")
            # 중국어 간체('ch_sim'), 중국어 번체('ch_tra'), 영어('en') 지원
            reader = easyocr.Reader(['ch_sim', 'en'], gpu=False)
            logger.info("EasyOCR 리더 초기화 완료")
            return reader
        except Exception as e:
            logger.warning(f"GPU 모드로 EasyOCR 초기화 실패, CPU 모드로 재시도: {e}")
            try:
                reader = easyocr.Reader(['ch_sim', 'ch_tra', 'en'], gpu=False)
                logger.info("EasyOCR 리더 초기화 완료 (CPU 모드)")
                return reader
            except Exception as e:
                logger.error(f"EasyOCR 리더 초기화 실패: {e}")
                raise

    def _extract_chinese_text(self, image_path):
        """이미지에서 중국어 텍스트 추출 (파일 경로)"""
        try:
            # EasyOCR로 텍스트 추출
            results = self.ocr_reader.readtext(image_path)

            # 결과에서 텍스트만 추출하여 합치기
            extracted_text = self._process_ocr_results(results)

            logger.info(f"EasyOCR로 추출된 텍스트: {extracted_text}")
            return extracted_text

        except Exception as e:
            logger.error(f"Error during OCR: {e}")
            raise

    def _extract_chinese_text_from_bytes(self, image_data: bytes):
        """이미지에서 중국어 텍스트 추출 (바이트 데이터)"""
        try:
            # 바이트 데이터를 PIL Image로 변환
            image = Image.open(io.BytesIO(image_data))

            # PIL Image를 numpy array로 변환 (EasyOCR이 요구하는 형식)
            image_array = np.array(image)

            # EasyOCR로 텍스트 추출
            results = self.ocr_reader.readtext(image_array)

            # 결과에서 텍스트만 추출하여 합치기
            extracted_text = self._process_ocr_results(results)

            logger.info(f"EasyOCR로 추출된 텍스트: {extracted_text}")
            return extracted_text

        except Exception as e:
            logger.error(f"Error during OCR from bytes: {e}")
            raise

    def _process_ocr_results(self, results):
        """EasyOCR 결과를 처리하여 텍스트 추출"""
        try:
            if not results:
                logger.warning("이미지에서 텍스트를 찾을 수 없습니다")
                return ""

            # EasyOCR 결과는 [bbox, text, confidence] 형태의 리스트
            # confidence가 0.5 이상인 텍스트만 추출
            texts = []
            for (bbox, text, confidence) in results:
                if confidence > 0.5:  # 신뢰도 임계값
                    texts.append(text)
                    logger.debug(f"추출된 텍스트: '{text}' (신뢰도: {confidence:.2f})")

            # 모든 텍스트를 줄바꿈으로 연결
            extracted_text = '\n'.join(texts)
            return extracted_text.strip()

        except Exception as e:
            logger.error(f"OCR 결과 처리 오류: {e}")
            return ""

    def translate_to_korean(self, text):
        """중국어 텍스트를 한국어로 번역"""
        if not text:
            return ""

        try:
            result = self.translator.translate(text)
            # None 체크 추가
            return result if result is not None else ""
        except Exception as e:
            logger.error(f"Error during translation: {e}")
            return ""  # 에러 시 빈 문자열 반환

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
        # None 체크 추가
        korean_text = korean_text if korean_text is not None else ""
        logger.info("번역된 한국어 텍스트: " + korean_text)

        return {
            "chinese_text": chinese_text,
            "korean_text": korean_text,
            "success": True
        }

    def set_confidence_threshold(self, threshold: float):
        """신뢰도 임계값 설정 (0.0 ~ 1.0)"""
        if 0.0 <= threshold <= 1.0:
            self.confidence_threshold = threshold
            logger.info(f"신뢰도 임계값을 {threshold}로 설정")
        else:
            logger.warning("신뢰도 임계값은 0.0과 1.0 사이의 값이어야 합니다")
