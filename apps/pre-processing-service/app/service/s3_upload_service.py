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
    """6단계: 크롤링된 상품 이미지들을 S3에 업로드하는 서비스"""

    def __init__(self):
        self.s3_util = S3UploadUtil()

    async def upload_crawled_products_to_s3(self, request: RequestS3Upload) -> dict:
        """
        크롤링된 상품들의 이미지를 S3에 업로드하는 비즈니스 로직 (6단계)
        """
        crawled_products = request.crawled_products
        base_folder = request.base_folder or "product-images"

        logger.info(f"S3 업로드 서비스 시작: {len(crawled_products)}개 상품")

        upload_results = []
        total_success_images = 0
        total_fail_images = 0
        processed_products = 0

        try:
            # HTTP 세션을 사용한 이미지 다운로드
            async with aiohttp.ClientSession() as session:

                # 각 상품별로 순차 업로드
                for product_info in crawled_products:
                    product_index = product_info.get("index", 0)
                    product_detail = product_info.get("product_detail")

                    logger.info(f"상품 {product_index}/{len(crawled_products)} S3 업로드 시작")

                    # 크롤링 실패한 상품은 스킵
                    if not product_detail or product_info.get("status") != "success":
                        logger.warning(f"상품 {product_index}: 크롤링 실패로 인한 업로드 스킵")
                        upload_results.append({
                            "product_index": product_index,
                            "product_title": "Unknown",
                            "product_url": product_info.get("url", ""),
                            "status": "skipped",
                            "reason": "크롤링 실패",
                            "success_count": 0,
                            "fail_count": 0,
                            "uploaded_images": [],
                            "failed_images": []
                        })
                        continue

                    try:
                        # 상품 이미지 업로드 (유틸리티 사용)
                        upload_result = await self.s3_util.upload_single_product_images(
                            session, product_detail, product_index, base_folder
                        )

                        upload_results.append(upload_result)
                        total_success_images += upload_result["success_count"]
                        total_fail_images += upload_result["fail_count"]
                        processed_products += 1

                        logger.success(
                            f"상품 {product_index} S3 업로드 완료: 성공 {upload_result['success_count']}개, "
                            f"실패 {upload_result['fail_count']}개"
                        )

                    except Exception as e:
                        logger.error(f"상품 {product_index} S3 업로드 오류: {e}")
                        upload_results.append({
                            "product_index": product_index,
                            "product_title": product_detail.get("title", "Unknown"),
                            "product_url": product_detail.get("url", ""),
                            "status": "error",
                            "error": str(e),
                            "success_count": 0,
                            "fail_count": 0,
                            "uploaded_images": [],
                            "failed_images": []
                        })

                    # 상품간 간격 (서버 부하 방지)
                    if product_index < len(crawled_products):
                        await asyncio.sleep(1)

            logger.success(
                f"S3 업로드 서비스 완료: 처리된 상품 {processed_products}개, "
                f"총 성공 이미지 {total_success_images}개, 총 실패 이미지 {total_fail_images}개"
            )

            # 응답 데이터 구성
            data = {
                "upload_results": upload_results,
                "summary": {
                    "total_products": len(crawled_products),
                    "processed_products": processed_products,
                    "skipped_products": len(crawled_products) - processed_products,
                    "total_success_images": total_success_images,
                    "total_fail_images": total_fail_images,
                    "success_rate": f"{total_success_images}/{total_success_images + total_fail_images}" if (
                                                                                                                        total_success_images + total_fail_images) > 0 else "0/0"
                },
                "base_folder": base_folder,
                "uploaded_at": time.strftime("%Y-%m-%d %H:%M:%S"),
            }

            message = f"S3 업로드 완료: {total_success_images}개 이미지 업로드 성공"
            return Response.ok(data, message)

        except Exception as e:
            logger.error(f"S3 업로드 서비스 전체 오류: {e}")
            raise InvalidItemDataException()

    async def get_upload_status(self, upload_results: List[Dict]) -> Dict:
        """
        업로드 결과 상태 요약 (선택적 기능)
        """
        try:
            total_products = len(upload_results)
            successful_products = len([r for r in upload_results if r.get("status") == "completed"])
            failed_products = len([r for r in upload_results if r.get("status") in ["error", "skipped"]])

            total_images = sum(r.get("success_count", 0) + r.get("fail_count", 0) for r in upload_results)
            successful_images = sum(r.get("success_count", 0) for r in upload_results)
            failed_images = sum(r.get("fail_count", 0) for r in upload_results)

            status_summary = {
                "products": {
                    "total": total_products,
                    "successful": successful_products,
                    "failed": failed_products,
                    "success_rate": f"{successful_products}/{total_products}"
                },
                "images": {
                    "total": total_images,
                    "successful": successful_images,
                    "failed": failed_images,
                    "success_rate": f"{successful_images}/{total_images}" if total_images > 0 else "0/0"
                }
            }

            return status_summary

        except Exception as e:
            logger.error(f"업로드 상태 요약 오류: {e}")
            return {}