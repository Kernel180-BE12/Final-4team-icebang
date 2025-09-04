# app/service/crawl_service.py
import time
from app.utils.crawler_utils import DetailCrawler
from app.errors.CustomException import InvalidItemDataException
from app.model.schemas import RequestSadaguCrawl


async def crawl_product_detail(request: RequestSadaguCrawl) -> dict:
    """
    선택된 상품의 상세 정보를 크롤링하는 비즈니스 로직입니다. (5단계)
    상품 URL을 입력받아 상세 정보를 크롤링하여 딕셔너리로 반환합니다.
    """
    crawler = DetailCrawler(use_selenium=request.use_selenium)

    try:
        print(f"상품 상세 크롤링 시작: {request.product_url}")

        # 상세 정보 크롤링 실행
        product_detail = await crawler.crawl_detail(
            product_url=str(request.product_url),
            include_images=request.include_images
        )

        if not product_detail:
            raise InvalidItemDataException("상품 상세 정보 크롤링 실패")

        print(f"크롤링 완료: {product_detail.get('title', 'Unknown')[:50]}")

        # 응답 데이터 구성
        response_data = {
            "job_id": request.job_id,
            "schedule_id": request.schedule_id,
            "schedule_his_id": request.schedule_his_id,
            "tag": request.tag,
            "product_url": str(request.product_url),
            "use_selenium": request.use_selenium,
            "include_images": request.include_images,
            "product_detail": product_detail,
            "status": "success",
            "crawled_at": time.strftime('%Y-%m-%d %H:%M:%S')
        }

        return response_data

    except Exception as e:
        print(f"크롤링 서비스 오류: {e}")
        raise InvalidItemDataException(f"상품 상세 크롤링 오류: {e}")
    finally:
        await crawler.close()