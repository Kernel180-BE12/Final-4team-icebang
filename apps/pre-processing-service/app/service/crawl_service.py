# app/service/crawl_service.py (더 나은 방식)
import json
import time
import re
from bs4 import BeautifulSoup

from app.utils.crawler_utils import SearchCrawler
from app.errors.CustomException import InvalidItemDataException
from app.model.schemas import RequestSadaguCrawl


async def crawl_product_detail(request: RequestSadaguCrawl) -> dict:
    """
    선택된 상품의 상세 정보를 크롤링하는 비즈니스 로직입니다. (5단계)
    상품 URL을 입력받아 상세 정보를 크롤링하여 딕셔너리로 반환합니다.
    """
    # SearchCrawler를 재사용하되 상세 크롤링 기능 확장
    crawler = DetailCrawlerExtension(use_selenium=request.use_selenium)

    try:
        print(f"상품 상세 크롤링 시작: {request.product_url}")

        # 상세 정보 크롤링 실행
        product_detail = await crawler.crawl_detail(
            product_url=str(request.product_url),  # HttpUrl을 문자열로 변환
            include_images=request.include_images
        )

        if not product_detail:
            raise InvalidItemDataException("상품 상세 정보 크롤링 실패")

        print(f"크롤링 완료: {product_detail.get('title', 'Unknown')[:50]}")

        # 응답 데이터 구성
        response_data = {
            "job_id": request.job_id,
            "schedule_id": request.schedule_id,
            "sschdule_his_id": request.sschdule_his_id,
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


class DetailCrawlerExtension(SearchCrawler):
    """SearchCrawler를 확장한 상세 크롤링 클래스"""

    async def crawl_detail(self, product_url: str, include_images: bool = False) -> dict:
        """상품 상세 정보 크롤링"""
        try:
            if self.use_selenium:
                soup = await self._get_soup_selenium(product_url)
            else:
                soup = await self._get_soup_httpx(product_url)

            # 기본 정보 추출
            title = self._extract_title(soup)
            price = self._extract_price(soup)
            rating = self._extract_rating(soup)
            options = self._extract_options(soup)
            material_info = self._extract_material_info(soup)

            product_data = {
                'url': product_url,
                'title': title,
                'price': price,
                'rating': rating,
                'options': options,
                'material_info': material_info,
                'crawled_at': time.strftime('%Y-%m-%d %H:%M:%S')
            }

            # 이미지 정보 추가 (선택적)
            if include_images:
                print("이미지 정보 추출 중...")
                product_images = self._extract_images(soup)
                product_data['product_images'] = [{'original_url': img_url} for img_url in product_images]
                print(f"추출된 이미지: {len(product_images)}개")
            else:
                product_data['product_images'] = []

            return product_data

        except Exception as e:
            print(f"크롤링 오류: {e}")
            raise InvalidItemDataException(f"크롤링 실패: {str(e)}")

    async def _get_soup_selenium(self, product_url: str) -> BeautifulSoup:
        """Selenium으로 HTML 가져오기"""
        try:
            self.driver.get(product_url)
            self.wait.until(lambda driver: driver.execute_script("return document.readyState") == "complete")
            time.sleep(2)  # 추가 로딩 대기
            return BeautifulSoup(self.driver.page_source, 'html.parser')
        except Exception as e:
            raise Exception(f"Selenium HTML 로딩 실패: {e}")

    async def _get_soup_httpx(self, product_url: str) -> BeautifulSoup:
        """httpx로 HTML 가져오기"""
        try:
            response = await self.client.get(product_url)
            response.raise_for_status()
            return BeautifulSoup(response.content, 'html.parser')
        except Exception as e:
            raise Exception(f"HTTP 요청 실패: {e}")

    # 이하 모든 extract 메서드들은 동일...