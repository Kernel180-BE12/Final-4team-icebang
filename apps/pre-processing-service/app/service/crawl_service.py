import time
import asyncio
from app.service.crawlers.detail_crawler import DetailCrawler
from app.errors.CustomException import InvalidItemDataException
from app.model.schemas import RequestSadaguCrawl
from loguru import logger
from app.utils.response import Response


class CrawlService:
    def __init__(self):
        pass

    async def crawl_product_detail(self, request: RequestSadaguCrawl) -> dict:
        """
        선택된 상품들의 상세 정보를 크롤링하는 비즈니스 로직입니다. (5단계)
        여러 상품 URL을 입력받아 순차적으로 상세 정보를 크롤링하여 딕셔너리로 반환합니다.
        """
        product_urls = [str(url) for url in request.product_urls]

        logger.info(f"상품 상세 크롤링 서비스 시작: 총 {len(product_urls)}개 상품")

        crawled_products = []
        success_count = 0
        fail_count = 0

        try:
            # 각 상품을 순차적으로 크롤링 (안정성 확보)
            for i, product_url in enumerate(product_urls, 1):
                logger.info(f"상품 {i}/{len(product_urls)} 크롤링 시작: {product_url}")

                crawler = DetailCrawler(use_selenium=True)

                try:
                    # 상세 정보 크롤링 실행
                    product_detail = await crawler.crawl_detail(product_url)

                    if product_detail:
                        product_title = product_detail.get("title", "Unknown")[:50]
                        logger.success(
                            f"상품 {i} 크롤링 성공: title='{product_title}', price={product_detail.get('price', 0)}"
                        )

                        # 성공한 상품 추가
                        crawled_products.append(
                            {
                                "index": i,
                                "url": product_url,
                                "product_detail": product_detail,
                                "status": "success",
                                "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
                            }
                        )
                        success_count += 1
                    else:
                        logger.error(f"상품 {i} 크롤링 실패: 상세 정보 없음")
                        crawled_products.append(
                            {
                                "index": i,
                                "url": product_url,
                                "product_detail": None,
                                "status": "failed",
                                "error": "상세 정보 없음",
                                "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
                            }
                        )
                        fail_count += 1

                except Exception as e:
                    logger.error(
                        f"상품 {i} 크롤링 오류: url={product_url}, error='{e}'"
                    )
                    crawled_products.append(
                        {
                            "index": i,
                            "url": product_url,
                            "product_detail": None,
                            "status": "failed",
                            "error": str(e),
                            "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
                        }
                    )
                    fail_count += 1

                finally:
                    # 각 크롤러 개별 정리
                    await crawler.close()

                # 상품간 간격 (서버 부하 방지)
                if i < len(product_urls):
                    await asyncio.sleep(1)

            logger.success(
                f"전체 크롤링 완료: 총 {len(product_urls)}개, 성공 {success_count}개, 실패 {fail_count}개"
            )

            # 응답 데이터 구성
            data = {
                "crawled_products": crawled_products,
                "success_count": success_count,
                "fail_count": fail_count,
                "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
            }

            logger.info(
                f"상품 상세 크롤링 서비스 완료: success_rate={success_count}/{len(product_urls)}"
            )
            return Response.ok(data)

        except Exception as e:
            logger.error(f"배치 크롤링 서비스 오류: error='{e}'")
            raise InvalidItemDataException()

    # 기존 단일 크롤링 메서드도 유지 (하위 호환성)
    async def crawl_single_product_detail(self, product_url: str) -> dict:
        """
        단일 상품 크롤링 (하위 호환성용)
        """
        crawler = DetailCrawler(use_selenium=True)

        try:
            logger.info(f"단일 상품 크롤링 시작: {product_url}")

            product_detail = await crawler.crawl_detail(product_url)

            if not product_detail:
                logger.error(f"상품 상세 정보 크롤링 실패: url={product_url}")
                raise InvalidItemDataException()

            product_title = product_detail.get("title", "Unknown")[:50]
            logger.success(f"크롤링 완료: title='{product_title}'")

            data = {
                "product_url": product_url,
                "product_detail": product_detail,
                "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
            }

            return Response.ok(data)

        except Exception as e:
            logger.error(f"단일 크롤링 오류: url={product_url}, error='{e}'")
            raise InvalidItemDataException()
        finally:
            await crawler.close()
