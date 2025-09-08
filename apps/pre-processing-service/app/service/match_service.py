from app.utils.keyword_matcher import KeywordMatcher
from app.errors.CustomException import InvalidItemDataException
from ..model.schemas import RequestSadaguMatch


class MatchService:
    def __init__(self):
        pass

    def match_products(self, request: RequestSadaguMatch) -> dict:
        """
        키워드 매칭 로직 (MeCab 등 사용) - 3단계
        """
        keyword = request.keyword
        products = request.search_results

        if not products:
            return {
                "job_id": request.job_id,
                "schedule_id": request.schedule_id,
                "schedule_his_id": request.schedule_his_id,
                "keyword": keyword,
                "matched_products": [],
                "status": "success"
            }

        try:
            matcher = KeywordMatcher()
            matched_products = []

            print(f"키워드 '{keyword}'와 {len(products)}개 상품 매칭 분석 시작...")

            for i, product in enumerate(products):
                title = product.get('title', '')
                if not title:
                    continue

                # 키워드 매칭 분석
                match_result = matcher.analyze_keyword_match(title, keyword)

                print(f"상품 {i + 1}: {title[:50]} | {match_result['reason']}")

                if match_result['is_match']:
                    # 매칭된 상품에 매칭 정보 추가
                    matched_product = product.copy()
                    matched_product['match_info'] = {
                        'match_type': match_result['match_type'],
                        'match_score': match_result['score'],
                        'match_reason': match_result['reason']
                    }
                    matched_products.append(matched_product)
                    print(f"  ✅ 매칭됨!")

            print(f"매칭 결과: {len(matched_products)}개 상품")

            # 매칭 스코어 기준으로 정렬 (높은 순)
            matched_products.sort(key=lambda x: x['match_info']['match_score'], reverse=True)

            return {
                "job_id": request.job_id,
                "schedule_id": request.schedule_id,
                "schedule_his_id": request.schedule_his_id,
                "keyword": keyword,
                "matched_products": matched_products,
                "status": "success"
            }

        except Exception as e:
            print(f"매칭 서비스 오류: {e}")
            raise InvalidItemDataException(f"키워드 매칭 실패: {str(e)}")