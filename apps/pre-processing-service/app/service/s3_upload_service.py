import time
import asyncio
import aiohttp
from typing import List, Dict
from loguru import logger
from app.errors.CustomException import InvalidItemDataException
from app.model.schemas import RequestS3Upload
from app.utils.s3_upload_util import S3UploadUtil
from app.utils.response import Response


class S3UploadService:
    """6단계: 크롤링된 상품 이미지들과 데이터를 S3에 업로드하는 서비스"""

    def __init__(self):
        self.s3_util = S3UploadUtil()

    async def upload_crawled_products_to_s3(self, request: RequestS3Upload) -> dict:
        """
        크롤링된 상품들의 이미지와 데이터를 S3에 업로드하는 비즈니스 로직 (6단계)
        """
        keyword = request.keyword  # 키워드 추가
        crawled_products = request.crawled_products
        base_folder = request.base_folder or "product"  # 🔸 기본값 변경: product-images → product

        logger.info(f"S3 업로드 서비스 시작: keyword='{keyword}', {len(crawled_products)}개 상품")

        upload_results = []
        total_success_images = 0
        total_fail_images = 0

        try:
            # HTTP 세션을 사용한 이미지 다운로드
            async with aiohttp.ClientSession() as session:

                # 각 상품별로 순차 업로드
                for product_info in crawled_products:
                    product_index = product_info.get("index", 0)
                    product_detail = product_info.get("product_detail")

                    logger.info(
                        f"상품 {product_index}/{len(crawled_products)} S3 업로드 시작"
                    )

                    # 크롤링 실패한 상품은 스킵
                    if not product_detail or product_info.get("status") != "success":
                        logger.warning(
                            f"상품 {product_index}: 크롤링 실패로 인한 업로드 스킵"
                        )
                        upload_results.append(
                            {
                                "product_index": product_index,
                                "product_title": "Unknown",
                                "status": "skipped",
                                "folder_s3_url": None,
                                "uploaded_images": [],
                                "success_count": 0,
                                "fail_count": 0,
                            }
                        )
                        continue

                    try:
                        # 상품 이미지 + 데이터 업로드 (키워드 전달 추가!)
                        # 🔸 전체 크롤링 데이터를 전달 (product_detail이 아닌 product_info 전체)
                        upload_result = await self.s3_util.upload_single_product_images(
                            session, product_info, product_index, keyword, base_folder  # product_detail → product_info
                        )

                        upload_results.append(upload_result)
                        total_success_images += upload_result["success_count"]
                        total_fail_images += upload_result["fail_count"]

                        logger.success(
                            f"상품 {product_index} S3 업로드 완료: 성공 {upload_result['success_count']}개, "
                            f"실패 {upload_result['fail_count']}개"
                        )

                    except Exception as e:
                        logger.error(f"상품 {product_index} S3 업로드 오류: {e}")
                        upload_results.append(
                            {
                                "product_index": product_index,
                                "product_title": product_detail.get("title", "Unknown"),
                                "status": "error",
                                "folder_s3_url": None,
                                "uploaded_images": [],
                                "success_count": 0,
                                "fail_count": 0,
                            }
                        )

                    # 상품간 간격 (서버 부하 방지)
                    if product_index < len(crawled_products):
                        await asyncio.sleep(1)

            logger.success(
                f"S3 업로드 서비스 완료: 총 성공 이미지 {total_success_images}개, 총 실패 이미지 {total_fail_images}개"
            )

            # 간소화된 응답 데이터 구성
            data = {
                "upload_results": upload_results,
                "summary": {
                    "total_products": len(crawled_products),
                    "total_success_images": total_success_images,
                    "total_fail_images": total_fail_images,
                },
                "uploaded_at": time.strftime("%Y-%m-%d %H:%M:%S"),
            }

            message = f"S3 업로드 완료: {total_success_images}개 이미지 업로드 성공, 상품 데이터 JSON 파일 포함"
            return Response.ok(data, message)

        except Exception as e:
            logger.error(f"S3 업로드 서비스 전체 오류: {e}")
            raise InvalidItemDataException()