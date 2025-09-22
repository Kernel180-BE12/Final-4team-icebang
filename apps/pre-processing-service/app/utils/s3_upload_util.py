import os
import json
import boto3
import aiohttp
import asyncio
from datetime import datetime
from urllib.parse import urlparse
from typing import Dict, Optional
from loguru import logger


class S3UploadUtil:
    """S3 업로드 전용 유틸리티 클래스"""

    def __init__(self):
        # 환경변수에서 AWS 설정 읽기
        self.aws_access_key = os.getenv("AWS_ACCESS_KEY_ID")
        self.aws_secret_key = os.getenv("AWS_SECRET_ACCESS_KEY")
        self.bucket_name = os.getenv("S3_BUCKET_NAME", "icebang4-dev-bucket")
        self.region = os.getenv("AWS_REGION", "ap-northeast-2")

        if not self.aws_access_key or not self.aws_secret_key:
            raise ValueError(
                "AWS_ACCESS_KEY_ID와 AWS_SECRET_ACCESS_KEY 환경변수가 필요합니다"
            )

        self.base_url = f"https://{self.bucket_name}.s3.{self.region}.amazonaws.com"

        # S3 클라이언트 초기화
        self.s3_client = boto3.client(
            "s3",
            aws_access_key_id=self.aws_access_key,
            aws_secret_access_key=self.aws_secret_key,
            region_name=self.region,
        )

        logger.info(
            f"S3 클라이언트 초기화 완료: bucket={self.bucket_name}, region={self.region}"
        )

    async def download_image(
            self, session: aiohttp.ClientSession, image_url: str
    ) -> Optional[bytes]:
        """이미지 URL에서 이미지 데이터 다운로드"""
        try:
            logger.debug(f"이미지 다운로드 시작: {image_url}")

            async with session.get(
                    image_url, timeout=aiohttp.ClientTimeout(total=30)
            ) as response:
                if response.status == 200:
                    image_data = await response.read()
                    logger.debug(f"이미지 다운로드 완료: {len(image_data)} bytes")
                    return image_data
                else:
                    logger.warning(
                        f"이미지 다운로드 실패: {image_url}, status={response.status}"
                    )
                    return None

        except Exception as e:
            logger.error(f"이미지 다운로드 오류: {image_url}, error={e}")
            return None

    def get_file_extension(self, image_url: str) -> str:
        """URL에서 파일 확장자 추출"""
        parsed = urlparse(image_url)
        path = parsed.path.lower()

        # 일반적인 이미지 확장자 확인
        for ext in [".jpg", ".jpeg", ".png", ".gif", ".webp"]:
            if ext in path:
                return ext

        # 기본값
        return ".jpg"

    def get_content_type(self, file_extension: str) -> str:
        """파일 확장자에 따른 Content-Type 반환"""
        content_types = {
            ".jpg": "image/jpeg",
            ".jpeg": "image/jpeg",
            ".png": "image/png",
            ".gif": "image/gif",
            ".webp": "image/webp",
        }
        return content_types.get(file_extension, "image/jpeg")

    def upload_to_s3(
            self, data: bytes, s3_key: str, content_type: str = "image/jpeg"
    ) -> bool:
        """S3에 데이터 업로드 (이미지 또는 JSON)"""
        try:
            logger.debug(f"S3 업로드 시작: key={s3_key}")

            self.s3_client.put_object(
                Bucket=self.bucket_name,
                Key=s3_key,
                Body=data,
                ContentType=content_type,
            )

            logger.debug(f"S3 업로드 완료: key={s3_key}")
            return True

        except Exception as e:
            logger.error(f"S3 업로드 오류: key={s3_key}, error={e}")
            return False

    def upload_json_to_s3(self, json_data: Dict, s3_key: str) -> bool:
        """JSON 데이터를 S3에 업로드"""
        try:
            json_str = json.dumps(json_data, ensure_ascii=False, indent=2)
            json_bytes = json_str.encode('utf-8')

            return self.upload_to_s3(json_bytes, s3_key, "application/json")

        except Exception as e:
            logger.error(f"JSON S3 업로드 오류: key={s3_key}, error={e}")
            return False

    def generate_product_folder_name(self, product_index: int, keyword: str) -> str:
        """상품별 폴더명 생성 (시간_키워드_번호)"""
        # 키워드에서 특수문자 제거
        safe_keyword = (
            keyword.replace("/", "-")
            .replace("\\", "-")
            .replace(" ", "_")
            .replace(":", "-")
            .replace("*", "-")
            .replace("?", "-")
            .replace('"', "-")
            .replace("<", "-")
            .replace(">", "-")
            .replace("|", "-")[:20]  # 길이 제한
        )

        # 날짜 형식: 20250922
        date_str = datetime.now().strftime("%Y%m%d")

        # 폴더명: 20250922_키워드_1
        folder_name = f"{date_str}_{safe_keyword}_{product_index}"

        return folder_name

    def generate_s3_key(
            self,
            base_folder: str,
            folder_name: str,
            file_name: str,
    ) -> str:
        """S3 키 생성"""
        # 최종 S3 키: product/20250922_산리오_1/image_001.jpg 또는 product_data.json
        s3_key = f"{base_folder}/{folder_name}/{file_name}"
        return s3_key

    def get_s3_url(self, s3_key: str) -> str:
        """S3 키에서 접근 가능한 URL 생성"""
        return f"{self.base_url}/{s3_key}"

    async def upload_single_product_images(
            self,
            session: aiohttp.ClientSession,
            product_info: Dict,  # 🔸 이름 변경: product_data → product_info (전체 크롤링 데이터)
            product_index: int,
            keyword: str,  # 키워드 파라미터 추가
            base_folder: str = "product",  # 🔸 기본 폴더 변경: product-images → product
    ) -> Dict:
        """단일 상품의 모든 데이터(이미지 + JSON)를 S3에 업로드"""

        # 🔸 전체 크롤링 데이터에서 필요한 정보 추출
        product_detail = product_info.get("product_detail", {})
        product_title = product_detail.get("title", "Unknown")
        product_images = product_detail.get("product_images", [])

        uploaded_images = []

        logger.info(
            f"상품 {product_index} 업로드 시작: {len(product_images)}개 이미지, keyword='{keyword}'"
        )

        # 키워드 기반 폴더명 한 번만 생성
        folder_name = self.generate_product_folder_name(product_index, keyword)

        fail_count = 0
        folder_s3_url = f"{self.base_url}/{base_folder}/{folder_name}"

        # 🆕 1. 먼저 상품 데이터 JSON 파일 업로드
        try:
            # 전체 크롤링 데이터를 JSON으로 저장 (S3 업로드 메타데이터 추가)
            product_data_with_meta = {
                **product_info,  # 전체 크롤링 데이터 (index, url, product_detail, status, crawled_at 포함)
                "s3_upload_keyword": keyword,  # 추가 메타데이터
                "s3_uploaded_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            }

            json_s3_key = self.generate_s3_key(base_folder, folder_name, "product_data.json")

            if self.upload_json_to_s3(product_data_with_meta, json_s3_key):
                logger.success(f"상품 {product_index} JSON 데이터 업로드 완료")
            else:
                logger.error(f"상품 {product_index} JSON 데이터 업로드 실패")

        except Exception as e:
            logger.error(f"상품 {product_index} JSON 업로드 오류: {e}")

        # 2. 이미지 업로드 (기존 로직)
        if not product_images:
            logger.warning(f"상품 {product_index}: 업로드할 이미지가 없음")
            return {
                "product_index": product_index,
                "product_title": product_title,
                "status": "no_images",
                "folder_s3_url": folder_s3_url,
                "uploaded_images": uploaded_images,
                "success_count": 0,
                "fail_count": 0,
            }

        # 각 이미지 업로드
        for img_idx, img_info in enumerate(product_images, 1):
            original_url = img_info.get("original_url", "")

            if not original_url:
                logger.warning(f"상품 {product_index}, 이미지 {img_idx}: URL이 없음")
                fail_count += 1
                continue

            try:
                # 이미지 다운로드
                image_data = await self.download_image(session, original_url)

                if not image_data:
                    fail_count += 1
                    continue

                # S3 키 생성 (키워드 기반 폴더명 사용)
                file_extension = self.get_file_extension(original_url)
                image_file_name = f"image_{img_idx:03d}{file_extension}"
                s3_key = self.generate_s3_key(base_folder, folder_name, image_file_name)

                # S3 업로드
                content_type = self.get_content_type(file_extension)

                if self.upload_to_s3(image_data, s3_key, content_type):
                    s3_url = self.get_s3_url(s3_key)
                    uploaded_images.append(
                        {
                            "index": img_idx,
                            "original_url": original_url,
                            "s3_url": s3_url,
                        }
                    )

                    logger.debug(f"상품 {product_index}, 이미지 {img_idx} 업로드 완료")
                else:
                    fail_count += 1

            except Exception as e:
                logger.error(f"상품 {product_index}, 이미지 {img_idx} 처리 오류: {e}")
                fail_count += 1

            # 이미지 간 간격 (서버 부하 방지)
            await asyncio.sleep(0.5)

        logger.success(
            f"상품 {product_index} 업로드 완료: 성공 {len(uploaded_images)}개, 실패 {fail_count}개, folder='{folder_name}'"
        )

        return {
            "product_index": product_index,
            "product_title": product_title,
            "status": "completed",
            "folder_s3_url": folder_s3_url,  # 🔸 폴더 전체를 가리킴 (이미지 + JSON 포함)
            "json_s3_url": f"{folder_s3_url}/product_data.json",  # 🆕 JSON 파일 직접 링크
            "uploaded_images": uploaded_images,
            "success_count": len(uploaded_images),
            "fail_count": fail_count,
        }