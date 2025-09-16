import time
from app.service.crawlers.detail_crawler import DetailCrawler
from app.errors.CustomException import InvalidItemDataException
from app.model.schemas import RequestSadaguCrawl
from loguru import logger


class CrawlService:
    def __init__(self):
        pass

    async def crawl_product_detail(self, request: RequestSadaguCrawl) -> dict:
        """
        선택된 상품의 상세 정보를 크롤링하는 비즈니스 로직입니다. (5단계)
        상품 URL을 입력받아 상세 정보를 크롤링하여 딕셔너리로 반환합니다.
        """
        crawler = DetailCrawler(use_selenium=True)

        try:
            logger.info(
                f"상품 상세 크롤링 서비스 시작: product_url={request.product_url}"
            )

            # 상세 정보 크롤링 실행
            product_detail = await crawler.crawl_detail(
                product_url=str(request.product_url), include_images=False
            )

            if not product_detail:
                logger.error(f"상품 상세 정보 크롤링 실패: url={request.product_url}")
                raise InvalidItemDataException()

            product_title = product_detail.get("title", "Unknown")[:50]
            logger.success(
                f"크롤링 완료: title='{product_title}', price={product_detail.get('price', 0)}, options_count={len(product_detail.get('options', []))}"
            )

            # 응답 데이터 구성
            response_data = {
                "tag": request.tag,
                "product_url": str(request.product_url),
                "product_detail": product_detail,
                "status": "success",
                "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
            }

            logger.info(
                f"상품 상세 크롤링 서비스 완료: status=success"
            )
            return response_data

        except Exception as e:
            logger.error(
                f"크롤링 서비스 오류: product_url={request.product_url}, error='{e}'"
            )
            raise InvalidItemDataException()
        finally:
            await crawler.close()
            logger.debug("크롤러 리소스 정리 완료")
