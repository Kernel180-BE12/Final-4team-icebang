from datetime import datetime

import boto3
import os
from loguru import logger
from typing import List
from dotenv import load_dotenv

load_dotenv()

class S3Service:

    def __init__(self, keyword:str):
        self.s3_client = boto3.client('s3',
                                      aws_access_key_id=os.getenv("AWS_ACCESS_KEY_ID"),
                                      aws_secret_access_key=os.getenv("AWS_SECRET_ACCESS_KEY"),
                                      region_name=os.getenv("AWS_REGION"))
        self.bucket_name = os.getenv("S3_BUCKET_NAME")
        self.date = datetime.now().strftime("%Y%m%d")
        self.keyword = keyword

    def get_folder_objects(self):
        """S3 버킷에서 특정 폴더 내의 모든 객체를 가져오는 메서드"""

        try:
            response = self.s3_client.list_objects_v2(
                Bucket=self.bucket_name,
                Prefix=f"product/20250922_{self.keyword}_1"
            )

            objects = []
            if 'Contents' in response:
                for obj in response['Contents']:
                    objects.append(obj['Key'])

            return objects
        except Exception as e:
            logger.error(f"S3 객체 조회 실패: {e}")
            return []

    def get_jpg_files(self, object_keys: List[str]) -> List[str]:
        """객체 키 리스트에서 JPG 파일만 필터링"""
        jpg_files = []
        for key in object_keys:
            if key.lower().endswith(('.jpg', '.jpeg')):
                jpg_files.append(key)
        return jpg_files

    def get_image_data(self, key: str) -> bytes:
        """S3에서 이미지 데이터 가져오기"""
        try:
            response = self.s3_client.get_object(Bucket=self.bucket_name, Key=key)
            image_data = response['Body'].read()
            return image_data
        except Exception as e:
            logger.error(f"S3 이미지 데이터 가져오기 실패 ({key}): {e}")
            raise