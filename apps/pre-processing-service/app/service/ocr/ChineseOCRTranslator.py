import os
import json
from google.cloud import vision
from google.oauth2 import service_account
from deep_translator import GoogleTranslator
from loguru import logger
import io


class ChineseOCRTranslator:

    def __init__(self):
        self.translator = GoogleTranslator(source='zh-CN', target='ko')

        # Google Vision API 클라이언트 초기화
        self.vision_client = self._initialize_vision_client()

    def _initialize_vision_client(self):
        """Google Vision API 클라이언트 초기화"""
        try:
            # # 방법 1: 환경변수에서 JSON 문자열로 인증정보 가져오기
            # creds_json = os.getenv('GOOGLE_APPLICATION_CREDENTIALS_JSON')
            # if creds_json:
            #     logger.info("환경변수에서 Google 인증정보 로드")
            #     creds_dict = json.loads(creds_json)
            #     if "private_key" in creds_dict:
            #         while "\\n" in creds_dict["private_key"]:
            #             creds_dict["private_key"] = creds_dict["private_key"].replace("\\n", "\n")
            #     credentials = service_account.Credentials.from_service_account_info(creds_dict)
            #     return vision.ImageAnnotatorClient(credentials=credentials)

            # 방법 2: 파일 경로에서 인증정보 가져오기
            creds_file = os.getenv('GOOGLE_APPLICATION_CREDENTIALS')
            if creds_file and os.path.exists(creds_file):
                logger.info(f"파일에서 Google 인증정보 로드: {creds_file}")
                return vision.ImageAnnotatorClient()

            # 방법 3: 기본 인증 (Cloud Shell, GCE 등)
            logger.info("기본 인증으로 Google Vision 클라이언트 초기화")
            return vision.ImageAnnotatorClient()

        except Exception as e:
            logger.error(f"Google Vision 클라이언트 초기화 실패: {e}")
            raise

    def _extract_chinese_text(self, image_path):
        """이미지에서 중국어 텍스트 추출 (파일 경로)"""
        try:
            # 이미지 파일 읽기
            with io.open(image_path, 'rb') as image_file:
                content = image_file.read()

            return self._extract_text_from_content(content)

        except Exception as e:
            logger.error(f"Error during OCR: {e}")
            raise

    def _extract_chinese_text_from_bytes(self, image_data: bytes):
        """이미지에서 중국어 텍스트 추출 (바이트 데이터)"""
        try:
            return self._extract_text_from_content(image_data)
        except Exception as e:
            logger.error(f"Error during OCR from bytes: {e}")
            raise

    def _extract_text_from_content(self, image_content: bytes):
        """Google Vision API를 사용해 이미지에서 텍스트 추출"""
        try:
            # Vision API Image 객체 생성
            image = vision.Image(content=image_content)

            # 텍스트 감지 요청
            response = self.vision_client.text_detection(image=image)

            # 에러 체크
            if response.error.message:
                raise Exception(f'Vision API Error: {response.error.message}')

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

if __name__ == "__main__":
    from S3Service import S3Service

    s3_service = S3Service(keyword="가디건")
    ocr = ChineseOCRTranslator()

    object_keys = s3_service.get_folder_objects()
    jpg_files = s3_service.get_jpg_files(object_keys)
    print(f"JPG 파일 {len(jpg_files)}개 발견")

    results = {}
    for key in jpg_files:
        print(f"Processing {key}...")
        image_data = s3_service.get_image_data(key)
        result = ocr.process_image_from_bytes(image_data)
        results[key] = result
        print(result)
        print("------")
