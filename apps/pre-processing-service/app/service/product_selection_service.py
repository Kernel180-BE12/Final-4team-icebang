import json
from typing import List, Dict
from loguru import logger
from app.model.schemas import RequestProductSelect
from app.utils.response import Response
from app.db.mariadb_manager import MariadbManager


class ProductSelectionService:
    """콘텐츠 생성용 단일 상품 선택 서비스"""

    def __init__(self):
        self.db_manager = MariadbManager()

    def select_product_for_content(self, request: RequestProductSelect) -> dict:
        """
        S3 업로드와 DB 저장 결과를 바탕으로 콘텐츠 생성용 단일 상품을 선택
        """
        try:
            task_run_id = request.task_run_id
            logger.info(f"콘텐츠용 상품 선택 시작: task_run_id={task_run_id}")

            # 1. DB에서 해당 task_run_id의 모든 상품 조회
            db_products = self._fetch_products_from_db(task_run_id)

            if not db_products:
                logger.warning(f"DB에서 상품을 찾을 수 없음: task_run_id={task_run_id}")
                return Response.error("상품 데이터를 찾을 수 없습니다.", "PRODUCTS_NOT_FOUND")

            # 2. 최적 상품 선택
            selected_product = self._select_best_product(db_products)

            logger.success(
                f"콘텐츠용 상품 선택 완료: name={selected_product['name']}, "
                f"selection_reason={selected_product['selection_reason']}"
            )

            data = {
                "task_run_id": task_run_id,
                "selected_product": selected_product,
                "total_available_products": len(db_products),
            }

            return Response.ok(data, f"콘텐츠용 상품 선택 완료: {selected_product['name']}")

        except Exception as e:
            logger.error(f"콘텐츠용 상품 선택 오류: {e}")
            raise

    def _fetch_products_from_db(self, task_run_id: int) -> List[Dict]:
        """DB에서 task_run_id에 해당하는 모든 상품 조회"""
        try:
            sql = """
                  SELECT id, \
                         name, \
                         data_value, \
                         created_at
                  FROM task_io_data
                  WHERE task_run_id = %s
                    AND io_type = 'OUTPUT'
                    AND data_type = 'JSON'
                  ORDER BY name \
                  """

            with self.db_manager.get_cursor() as cursor:
                cursor.execute(sql, (task_run_id,))
                rows = cursor.fetchall()

                products = []
                for row in rows:
                    try:
                        # MariaDB에서 반환되는 row는 튜플 형태
                        id, name, data_value_str, created_at = row

                        # JSON 데이터 파싱
                        data_value = json.loads(data_value_str)

                        products.append({
                            "id": id,
                            "name": name,
                            "data_value": data_value,
                            "created_at": created_at
                        })
                    except json.JSONDecodeError as e:
                        logger.warning(f"JSON 파싱 실패: name={name}, error={e}")
                        continue
                    except Exception as e:
                        logger.warning(f"Row 처리 실패: {row}, error={e}")
                        continue

            logger.info(f"DB에서 {len(products)}개 상품 조회 완료")
            return products

        except Exception as e:
            logger.error(f"DB 상품 조회 오류: {e}")
            return []

    def _select_best_product(self, db_products: List[Dict]) -> Dict:
        """
        상품 선택 로직:
        1순위: S3 이미지 업로드가 성공하고 이미지가 많은 상품
        2순위: 크롤링 성공한 첫 번째 상품
        3순위: 첫 번째 상품 (fallback)
        """
        try:
            successful_products = []

            # 1순위: S3 업로드 성공하고 이미지가 있는 상품들
            for product in db_products:
                data_value = product.get("data_value", {})
                product_detail = data_value.get("product_detail", {})
                product_images = product_detail.get("product_images", [])

                # 크롤링 성공하고 이미지가 있는 상품
                if (data_value.get("status") == "success" and
                        product_detail and len(product_images) > 0):
                    successful_products.append({
                        "product": product,
                        "image_count": len(product_images),
                        "title": product_detail.get("title", "Unknown")
                    })

            if successful_products:
                # 이미지 개수가 가장 많은 상품 선택
                best_product = max(successful_products, key=lambda x: x["image_count"])

                logger.info(
                    f"1순위 선택: name={best_product['product']['name']}, "
                    f"images={best_product['image_count']}개"
                )

                return {
                    "selection_reason": "s3_upload_success_with_most_images",
                    "name": best_product["product"]["name"],
                    "product_info": best_product["product"]["data_value"],
                    "image_count": best_product["image_count"],
                    "title": best_product["title"]
                }

            # 2순위: 크롤링 성공한 첫 번째 상품 (이미지 없어도)
            for product in db_products:
                data_value = product.get("data_value", {})
                if (data_value.get("status") == "success" and
                        data_value.get("product_detail")):
                    product_detail = data_value.get("product_detail", {})
                    logger.info(f"2순위 선택: name={product['name']}")

                    return {
                        "selection_reason": "first_crawl_success",
                        "name": product["name"],
                        "product_info": data_value,
                        "image_count": len(product_detail.get("product_images", [])),
                        "title": product_detail.get("title", "Unknown")
                    }

            # 3순위: 첫 번째 상품 (fallback)
            if db_products:
                first_product = db_products[0]
                data_value = first_product.get("data_value", {})
                product_detail = data_value.get("product_detail", {})

                logger.warning(f"3순위 fallback 선택: name={first_product['name']}")

                return {
                    "selection_reason": "fallback_first_product",
                    "name": first_product["name"],
                    "product_info": data_value,
                    "image_count": len(product_detail.get("product_images", [])),
                    "title": product_detail.get("title", "Unknown")
                }

            # 모든 경우 실패
            logger.error("선택할 상품이 없습니다")
            return {
                "selection_reason": "no_products_available",
                "name": None,
                "product_info": None,
                "image_count": 0,
                "title": "Unknown"
            }

        except Exception as e:
            logger.error(f"상품 선택 로직 오류: {e}")
            return {
                "selection_reason": "selection_error",
                "name": db_products[0]["name"] if db_products else None,
                "product_info": db_products[0]["data_value"] if db_products else None,
                "image_count": 0,
                "title": "Unknown",
                "error": str(e)
            }