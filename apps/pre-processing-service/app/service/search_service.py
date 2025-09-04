from app.utils.crawler_utils import SearchCrawler
from app.errors.CustomException import InvalidItemDataException
from ..model.schemas import RequestSadaguSearch


async def search_products(request: RequestSadaguSearch) -> dict:
    """
    키워드 기반으로 상품을 검색하는 비즈니스 로직 (2단계)
    """
    keyword = request.keyword
    crawler = SearchCrawler(use_selenium=True)

    try:
        print(f"키워드 '{keyword}'로 상품 검색 시작...")

        # Selenium 또는 httpx로 상품 검색
        if crawler.use_selenium:
            search_results = await crawler.search_products_selenium(keyword)
        else:
            search_results = await crawler.search_products_httpx(keyword)

        if not search_results:
            print("검색 결과가 없습니다.")
            return {
                "job_id": request.job_id,
                "schedule_id": request.schedule_id,
                "sschdule_his_id": request.sschdule_his_id,
                "keyword": keyword,
                "search_results": [],
                "status": "success"
            }

        # 상품별 기본 정보 수집 (제목이 없는 경우 다시 크롤링)
        enriched_results = []
        print(f"총 {len(search_results)}개 상품의 기본 정보를 수집 중...")

        for i, product in enumerate(search_results):
            try:
                # 이미 제목이 있고 유효한 경우 그대로 사용
                if product.get('title') and product['title'] != 'Unknown Title' and len(product['title'].strip()) > 0:
                    enriched_results.append(product)
                else:
                    # 제목이 없거나 유효하지 않은 경우 다시 크롤링
                    print(f"상품 {i + 1}: 제목 재수집 중... ({product['url']})")
                    basic_info = await crawler.get_basic_product_info(product['url'])

                    if basic_info and basic_info['title'] != "제목 없음":
                        enriched_results.append({
                            'url': product['url'],
                            'title': basic_info['title']
                        })
                    else:
                        # 그래도 제목을 못 찾으면 제외
                        print(f"  제목 추출 실패, 제외")
                        continue

                # 최대 20개까지만 처리
                if len(enriched_results) >= 20:
                    break

            except Exception as e:
                print(f"상품 {i + 1} 처리 중 오류: {e}")
                continue

        print(f"최종 수집된 유효 상품: {len(enriched_results)}개")

        return {
            "job_id": request.job_id,
            "schedule_id": request.schedule_id,
            "sschdule_his_id": request.sschdule_his_id,
            "keyword": keyword,
            "search_results": enriched_results,
            "status": "success"
        }

    except Exception as e:
        print(f"검색 서비스 오류: {e}")
        raise InvalidItemDataException(f"상품 검색 실패: {str(e)}")

    finally:
        await crawler.close()