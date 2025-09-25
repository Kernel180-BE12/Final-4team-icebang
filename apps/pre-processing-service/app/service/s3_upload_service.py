import time
import json
import asyncio
import aiohttp
import ssl, certifi
from typing import List, Dict
from datetime import datetime
from loguru import logger
from app.errors.CustomException import InvalidItemDataException
from app.model.schemas import RequestS3Upload
from app.utils.s3_upload_util import S3UploadUtil
from app.utils.response import Response
from app.db.mariadb_manager import MariadbManager


class S3UploadService:
    """6단계: 크롤링된 상품 이미지들과 데이터를 S3에 업로드하고 DB에 저장하는 서비스"""

    def __init__(self):
        self.s3_util = S3UploadUtil()
        self.db_manager = MariadbManager()

    async def upload_crawled_products_to_s3(self, request: RequestS3Upload) -> dict:
        """
        크롤링된 상품들의 이미지와 데이터를 S3에 업로드하고 DB에 저장하는 비즈니스 로직 (6단계)
        """
        keyword = request.keyword
        crawled_products = request.crawled_products
        base_folder = request.base_folder or "product"

        # task_run_id는 자바 워크플로우에서 전달받음
        task_run_id = getattr(request, 'task_run_id', None)
        if not task_run_id:
            # 임시: task_run_id가 없으면 생성
            task_run_id = int(time.time() * 1000)
            logger.warning(f"task_run_id가 없어서 임시로 생성: {task_run_id}")
        else:
            logger.info(f"자바 워크플로우에서 전달받은 task_run_id: {task_run_id}")

        logger.info(
            f"S3 업로드 + DB 저장 서비스 시작: keyword='{keyword}', "
            f"{len(crawled_products)}개 상품, task_run_id={task_run_id}"
        )

        upload_results = []
        total_success_images = 0
        total_fail_images = 0
        db_save_results = []

        try:
            # HTTP 세션을 사용한 이미지 다운로드

            ssl_context = ssl.create_default_context(cafile=certifi.where())
            connector = aiohttp.TCPConnector(ssl=ssl_context)

            async with aiohttp.ClientSession(connector=connector) as session:

                # 각 상품별로 순차 업로드
                for product_info in crawled_products:
                    product_index = product_info.get("index", 0)
                    product_detail = product_info.get("product_detail")

                    logger.info(
                        f"상품 {product_index}/{len(crawled_products)} S3 업로드 + DB 저장 시작"
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
                        db_save_results.append({
                            "product_index": product_index,
                            "db_status": "skipped",
                            "error": "크롤링 실패"
                        })
                        continue

                    try:
                        # 1. 상품 이미지 + 데이터 S3 업로드
                        upload_result = await self.s3_util.upload_single_product_images(
                            session,
                            product_info,
                            product_index,
                            keyword,
                            base_folder,
                        )

                        upload_results.append(upload_result)
                        total_success_images += upload_result["success_count"]
                        total_fail_images += upload_result["fail_count"]

                        # 2. DB에 상품 데이터 저장
                        db_result = self._save_product_to_db(
                            task_run_id,
                            keyword,
                            product_index,
                            product_info
                        )
                        db_save_results.append(db_result)

                        logger.success(
                            f"상품 {product_index} S3 업로드 + DB 저장 완료: "
                            f"이미지 성공 {upload_result['success_count']}개, DB {db_result['db_status']}"
                        )

                    except Exception as e:
                        logger.error(f"상품 {product_index} S3 업로드/DB 저장 오류: {e}")
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
                        db_save_results.append({
                            "product_index": product_index,
                            "db_status": "error",
                            "error": str(e)
                        })

                    # 상품간 간격 (서버 부하 방지)
                    if product_index < len(crawled_products):
                        await asyncio.sleep(1)

            logger.success(
                f"S3 업로드 + DB 저장 서비스 완료: 총 성공 이미지 {total_success_images}개, "
                f"총 실패 이미지 {total_fail_images}개"
            )

            # 응답 데이터 구성
            data = {
                "upload_results": upload_results,
                "db_save_results": db_save_results,
                "task_run_id": task_run_id,
                "summary": {
                    "total_products": len(crawled_products),
                    "total_success_images": total_success_images,
                    "total_fail_images": total_fail_images,
                    "db_success_count": len([r for r in db_save_results if r.get("db_status") == "success"]),
                    "db_fail_count": len([r for r in db_save_results if r.get("db_status") == "error"]),
                },
                "uploaded_at": time.strftime("%Y-%m-%d %H:%M:%S"),
            }

            message = f"S3 업로드 + DB 저장 완료: {total_success_images}개 이미지 성공, {len([r for r in db_save_results if r.get('db_status') == 'success'])}개 상품 DB 저장 성공"
            return Response.ok(data, message)

        except Exception as e:
            logger.error(f"S3 업로드 + DB 저장 서비스 전체 오류: {e}")
            raise InvalidItemDataException()

    def _save_product_to_db(
            self,
            task_run_id: int,
            keyword: str,
            product_index: int,
            product_info: Dict
    ) -> Dict:
        """
        상품 데이터를 TASK_IO_DATA 테이블에 저장 (MariaDB)
        """
        try:
            # 상품명 생성 (산리오_01 형식)
            product_name = f"{keyword}_{product_index:02d}"

            # data_value에 저장할 JSON 데이터 (전체 product_info)
            data_value_json = json.dumps(product_info, ensure_ascii=False)

            # 현재 시간
            created_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

            # MariaDB에 저장
            with self.db_manager.get_cursor() as cursor:
                sql = """
                      INSERT INTO task_io_data
                      (task_run_id, io_type, name, data_type, data_value, created_at)
                      VALUES (%s, %s, %s, %s, %s, %s) \
                      """

                cursor.execute(sql, (
                    task_run_id,
                    "OUTPUT",
                    product_name,
                    "JSON",
                    data_value_json,
                    created_at
                ))

            logger.success(f"상품 {product_index} DB 저장 성공: name={product_name}")

            return {
                "product_index": product_index,
                "product_name": product_name,
                "db_status": "success",
                "task_run_id": task_run_id,
            }

        except Exception as e:
            logger.error(f"상품 {product_index} DB 저장 오류: {e}")
            return {
                "product_index": product_index,
                "db_status": "error",
                "error": str(e)
            }