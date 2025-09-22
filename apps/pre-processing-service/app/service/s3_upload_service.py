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
        base_folder = (
                request.base_folder or "product"
        )  # 🔸 기본값 변경: product-images → product

        logger.info(
            f"S3 업로드 서비스 시작: keyword='{keyword}', {len(crawled_products)}개 상품"
        )

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
                            session,
                            product_info,
                            product_index,
                            keyword,
                            base_folder,  # product_detail → product_info
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

            # 🆕 임시: 콘텐츠 생성용 단일 상품 선택 로직
            selected_product_for_content = self._select_single_product_for_content(
                crawled_products, upload_results
            )

            logger.success(
                f"S3 업로드 서비스 완료: 총 성공 이미지 {total_success_images}개, 총 실패 이미지 {total_fail_images}개"
            )

            # 기존 응답 데이터 구성
            data = {
                "upload_results": upload_results,
                "summary": {
                    "total_products": len(crawled_products),
                    "total_success_images": total_success_images,
                    "total_fail_images": total_fail_images,
                },
                "uploaded_at": time.strftime("%Y-%m-%d %H:%M:%S"),
                # 🆕 임시: 콘텐츠 생성용 단일 상품만 추가 (나중에 삭제 예정)
                "selected_product_for_content": selected_product_for_content,
            }

            message = f"S3 업로드 완료: {total_success_images}개 이미지 업로드 성공, 상품 데이터 JSON 파일 포함"
            return Response.ok(data, message)

        except Exception as e:
            logger.error(f"S3 업로드 서비스 전체 오류: {e}")
            raise InvalidItemDataException()

    def _select_single_product_for_content(
            self, crawled_products: List[Dict], upload_results: List[Dict]
    ) -> Dict:
        """
        🆕 임시: 콘텐츠 생성을 위한 단일 상품 선택 로직
        우선순위: 1) S3 업로드 성공한 상품 중 이미지 개수가 많은 것
                 2) 없다면 크롤링 성공한 첫 번째 상품
        """
        try:
            # 1순위: S3 업로드 성공하고 이미지가 있는 상품들
            successful_uploads = [
                result for result in upload_results
                if result.get("status") == "completed" and result.get("success_count", 0) > 0
            ]

            if successful_uploads:
                # 이미지 개수가 가장 많은 상품 선택
                best_upload = max(successful_uploads, key=lambda x: x.get("success_count", 0))
                selected_index = best_upload["product_index"]

                # 원본 크롤링 데이터에서 해당 상품 찾기
                for product_info in crawled_products:
                    if product_info.get("index") == selected_index:
                        logger.info(
                            f"콘텐츠 생성용 상품 선택: index={selected_index}, "
                            f"title='{product_info.get('product_detail', {}).get('title', 'Unknown')[:30]}', "
                            f"images={best_upload.get('success_count', 0)}개"
                        )
                        return {
                            "selection_reason": "s3_upload_success_with_most_images",
                            "product_info": product_info,
                            "s3_upload_info": best_upload,
                        }

            # 2순위: 크롤링 성공한 첫 번째 상품 (S3 업로드 실패해도)
            for product_info in crawled_products:
                if (product_info.get("status") == "success" and
                        product_info.get("product_detail")):

                    # 해당 상품의 S3 업로드 정보 찾기
                    upload_info = None
                    for result in upload_results:
                        if result.get("product_index") == product_info.get("index"):
                            upload_info = result
                            break

                    logger.info(
                        f"콘텐츠 생성용 상품 선택 (fallback): index={product_info.get('index')}, "
                        f"title='{product_info.get('product_detail', {}).get('title', 'Unknown')[:30]}'"
                    )
                    return {
                        "selection_reason": "first_crawl_success",
                        "product_info": product_info,
                        "s3_upload_info": upload_info,
                    }

            # 3순위: 아무거나 (모든 상품이 실패한 경우)
            if crawled_products:
                logger.warning("모든 상품이 크롤링 실패 - 첫 번째 상품으로 fallback")
                return {
                    "selection_reason": "fallback_first_product",
                    "product_info": crawled_products[0],
                    "s3_upload_info": upload_results[0] if upload_results else None,
                }

            logger.error("선택할 상품이 없습니다")
            return {
                "selection_reason": "no_products_available",
                "product_info": None,
                "s3_upload_info": None,
            }

        except Exception as e:
            logger.error(f"단일 상품 선택 오류: {e}")
            return {
                "selection_reason": "selection_error",
                "product_info": crawled_products[0] if crawled_products else None,
                "s3_upload_info": upload_results[0] if upload_results else None,
                "error": str(e),
            }