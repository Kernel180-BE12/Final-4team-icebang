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

    async def crawl_product_detail(self, request: RequestSadaguCrawl, max_concurrent: int = 5) -> dict:
        """
        선택된 상품들의 상세 정보를 크롤링하는 비즈니스 로직입니다. (5단계)
        여러 상품 URL을 입력받아 비동기로 상세 정보를 크롤링하여 딕셔너리로 반환합니다.
        """
        product_urls = [str(url) for url in request.product_urls]

        logger.info(f"상품 상세 크롤링 서비스 시작: 총 {len(product_urls)}개 상품")

        crawled_products = []
        success_count = 0
        fail_count = 0

        try:
            # 세마포어로 동시 실행 수 제한
            semaphore = asyncio.Semaphore(max_concurrent)

            # 모든 크롤링 태스크를 동시에 실행
            tasks = []
            for i, product_url in enumerate(product_urls, 1):
                task = self._crawl_single_with_semaphore(semaphore, i, product_url, len(product_urls))
                tasks.append(task)

            # 모든 태스크 동시 실행 및 결과 수집
            results = await asyncio.gather(*tasks, return_exceptions=True)

            # 결과 정리
            for result in results:
                if isinstance(result, Exception):
                    logger.error(f"크롤링 태스크 오류: {result}")
                    crawled_products.append({
                        "index": len(crawled_products) + 1,
                        "url": "unknown",
                        "product_detail": None,
                        "status": "failed",
                        "error": str(result),
                        "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
                    })
                    fail_count += 1
                else:
                    crawled_products.append(result)
                    if result["status"] == "success":
                        success_count += 1
                    else:
                        fail_count += 1

            # 인덱스 순으로 정렬
            crawled_products.sort(key=lambda x: x["index"])

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

    async def _crawl_single_with_semaphore(self, semaphore: asyncio.Semaphore, index: int, product_url: str,
                                           total_count: int) -> dict:
        """
        세마포어를 사용한 단일 상품 크롤링
        """
        async with semaphore:
            logger.info(f"상품 {index}/{total_count} 크롤링 시작: {product_url}")

            crawler = DetailCrawler(use_selenium=True)

            try:
                # 상세 정보 크롤링 실행
                product_detail = await crawler.crawl_detail(product_url)

                if product_detail:
                    product_title = product_detail.get("title", "Unknown")[:50]
                    logger.success(
                        f"상품 {index} 크롤링 성공: title='{product_title}', price={product_detail.get('price', 0)}"
                    )

                    return {
                        "index": index,
                        "url": product_url,
                        "product_detail": product_detail,
                        "status": "success",
                        "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
                    }
                else:
                    logger.error(f"상품 {index} 크롤링 실패: 상세 정보 없음")
                    return {
                        "index": index,
                        "url": product_url,
                        "product_detail": None,
                        "status": "failed",
                        "error": "상세 정보 없음",
                        "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
                    }

            except Exception as e:
                logger.error(f"상품 {index} 크롤링 오류: url={product_url}, error='{e}'")
                return {
                    "index": index,
                    "url": product_url,
                    "product_detail": None,
                    "status": "failed",
                    "error": str(e),
                    "crawled_at": time.strftime("%Y-%m-%d %H:%M:%S"),
                }

            finally:
                # 각 크롤러 개별 정리
                await crawler.close()

    async def crawl_single_product_detail(self, product_url: str) -> dict:
        """
        단일 상품 크롤링
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