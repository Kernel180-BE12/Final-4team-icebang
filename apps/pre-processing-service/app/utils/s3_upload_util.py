import os
import boto3
import aiohttp
import asyncio
from datetime import datetime
from urllib.parse import urlparse
from typing import List, Dict, Optional, Tuple
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
            raise ValueError("AWS_ACCESS_KEY_ID와 AWS_SECRET_ACCESS_KEY 환경변수가 필요합니다")

        self.base_url = f"https://{self.bucket_name}.s3.{self.region}.amazonaws.com"

        # S3 클라이언트 초기화
        self.s3_client = boto3.client(
            's3',
            aws_access_key_id=self.aws_access_key,
            aws_secret_access_key=self.aws_secret_key,
            region_name=self.region
        )

        logger.info(f"S3 클라이언트 초기화 완료: bucket={self.bucket_name}, region={self.region}")

    async def download_image(self, session: aiohttp.ClientSession, image_url: str) -> Optional[bytes]:
        """이미지 URL에서 이미지 데이터 다운로드"""
        try:
            logger.debug(f"이미지 다운로드 시작: {image_url}")

            async with session.get(image_url, timeout=aiohttp.ClientTimeout(total=30)) as response:
                if response.status == 200:
                    image_data = await response.read()
                    logger.debug(f"이미지 다운로드 완료: {len(image_data)} bytes")
                    return image_data
                else:
                    logger.warning(f"이미지 다운로드 실패: {image_url}, status={response.status}")
                    return None

        except Exception as e:
            logger.error(f"이미지 다운로드 오류: {image_url}, error={e}")
            return None

    def get_file_extension(self, image_url: str) -> str:
        """URL에서 파일 확장자 추출"""
        parsed = urlparse(image_url)
        path = parsed.path.lower()

        # 일반적인 이미지 확장자 확인
        for ext in ['.jpg', '.jpeg', '.png', '.gif', '.webp']:
            if ext in path:
                return ext

        # 기본값
        return '.jpg'

    def get_content_type(self, file_extension: str) -> str:
        """파일 확장자에 따른 Content-Type 반환"""
        content_types = {
            '.jpg': 'image/jpeg',
            '.jpeg': 'image/jpeg',
            '.png': 'image/png',
            '.gif': 'image/gif',
            '.webp': 'image/webp'
        }
        return content_types.get(file_extension, 'image/jpeg')

    def upload_to_s3(self, image_data: bytes, s3_key: str, content_type: str = "image/jpeg") -> bool:
        """S3에 이미지 업로드"""
        try:
            logger.debug(f"S3 업로드 시작: key={s3_key}")

            self.s3_client.put_object(
                Bucket=self.bucket_name,
                Key=s3_key,
                Body=image_data,
                ContentType=content_type,
            )

            logger.debug(f"S3 업로드 완료: key={s3_key}")
            return True

        except Exception as e:
            logger.error(f"S3 업로드 오류: key={s3_key}, error={e}")
            return False

    def generate_s3_key(self, base_folder: str, product_index: int, product_title: str,
                        image_index: int, file_extension: str) -> str:
        """S3 키 생성"""
        # 상품 제목에서 특수문자 제거
        safe_title = product_title.replace("/", "-").replace("\\", "-").replace(" ", "_")[:30]

        # 타임스탬프 + 상품 정보로 폴더명 생성
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        folder_name = f"{timestamp}_product_{product_index}_{safe_title}"

        # 최종 S3 키
        s3_key = f"{base_folder}/{folder_name}/image_{image_index:03d}{file_extension}"

        return s3_key

    def get_s3_url(self, s3_key: str) -> str:
        """S3 키에서 접근 가능한 URL 생성"""
        return f"{self.base_url}/{s3_key}"

    async def upload_single_product_images(self,
                                           session: aiohttp.ClientSession,
                                           product_data: Dict,
                                           product_index: int,
                                           base_folder: str = "product-images") -> Dict:
        """단일 상품의 모든 이미지를 S3에 업로드"""

        product_title = product_data.get("title", "Unknown")
        product_url = product_data.get("url", "")
        product_images = product_data.get("product_images", [])

        uploaded_images = []
        failed_images = []

        if not product_images:
            logger.warning(f"상품 {product_index}: 업로드할 이미지가 없음")
            return {
                "product_index": product_index,
                "product_title": product_title,
                "product_url": product_url,
                "status": "no_images",
                "uploaded_images": uploaded_images,
                "failed_images": failed_images,
                "success_count": 0,
                "fail_count": 0,
                "upload_folder": None,
                "folder_s3_url": None
            }

        logger.info(f"상품 {product_index} 이미지 업로드 시작: {len(product_images)}개 이미지")

        # 각 이미지 업로드
        for img_idx, img_info in enumerate(product_images, 1):
            original_url = img_info.get("original_url", "")

            if not original_url:
                logger.warning(f"상품 {product_index}, 이미지 {img_idx}: URL이 없음")
                failed_images.append({
                    "index": img_idx,
                    "original_url": original_url,
                    "error": "URL이 없음"
                })
                continue

            try:
                # 이미지 다운로드
                image_data = await self.download_image(session, original_url)

                if not image_data:
                    failed_images.append({
                        "index": img_idx,
                        "original_url": original_url,
                        "error": "다운로드 실패"
                    })
                    continue

                # S3 키 생성
                file_extension = self.get_file_extension(original_url)
                s3_key = self.generate_s3_key(
                    base_folder, product_index, product_title, img_idx, file_extension
                )

                # S3 업로드
                content_type = self.get_content_type(file_extension)

                if self.upload_to_s3(image_data, s3_key, content_type):
                    s3_url = self.get_s3_url(s3_key)
                    uploaded_images.append({
                        "index": img_idx,
                        "original_url": original_url,
                        "s3_key": s3_key,
                        "s3_url": s3_url,
                        "file_size": len(image_data),
                        "content_type": content_type
                    })
                    logger.debug(f"상품 {product_index}, 이미지 {img_idx} 업로드 완료")
                else:
                    failed_images.append({
                        "index": img_idx,
                        "original_url": original_url,
                        "error": "S3 업로드 실패"
                    })

            except Exception as e:
                logger.error(f"상품 {product_index}, 이미지 {img_idx} 처리 오류: {e}")
                failed_images.append({
                    "index": img_idx,
                    "original_url": original_url,
                    "error": str(e)
                })

            # 이미지 간 간격 (서버 부하 방지)
            await asyncio.sleep(0.5)

        # 업로드 폴더 정보 계산
        upload_folder = None
        folder_s3_url = None
        if uploaded_images:
            first_s3_key = uploaded_images[0]["s3_key"]
            upload_folder = "/".join(first_s3_key.split("/")[:-1])  # 파일명 제거
            folder_s3_url = f"{self.base_url}/{upload_folder}"

        logger.success(f"상품 {product_index} 업로드 완료: 성공 {len(uploaded_images)}개, 실패 {len(failed_images)}개")

        return {
            "product_index": product_index,
            "product_title": product_title,
            "product_url": product_url,
            "status": "completed",
            "upload_folder": upload_folder,
            "folder_s3_url": folder_s3_url,
            "uploaded_images": uploaded_images,
            "failed_images": failed_images,
            "success_count": len(uploaded_images),
            "fail_count": len(failed_images)
        }