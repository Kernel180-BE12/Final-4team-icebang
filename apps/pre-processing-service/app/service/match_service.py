from app.utils.keyword_matcher import KeywordMatcher
from app.errors.CustomException import InvalidItemDataException
from ..model.schemas import RequestSadaguMatch
from loguru import logger


class MatchService:
    def __init__(self):
        pass

    def match_products(self, request: RequestSadaguMatch) -> dict:
        """
        키워드 매칭 로직 (MeCab 등 사용) - 3단계
        """
        keyword = request.keyword
        products = request.search_results

        logger.info(
            f"키워드 매칭 서비스 시작: job_id={request.job_id}, schedule_id={request.schedule_id}, keyword='{keyword}', products_count={len(products) if products else 0}"
        )

        if not products:
            logger.warning(f"매칭할 상품이 없음: keyword='{keyword}'")
            return {
                "job_id": request.job_id,
                "schedule_id": request.schedule_id,
                "schedule_his_id": request.schedule_his_id,
                "keyword": keyword,
                "matched_products": [],
                "status": "success",
            }

        try:
            matcher = KeywordMatcher()
            matched_products = []

            logger.info(
                f"키워드 '{keyword}'와 {len(products)}개 상품 매칭 분석 시작..."
            )

            for i, product in enumerate(products):
                title = product.get("title", "")
                if not title:
                    logger.debug(f"상품 {i + 1}: 제목이 없어서 스킵")
                    continue

                logger.debug(f"상품 {i + 1} 매칭 분석 시작: title='{title[:50]}'")

                # 키워드 매칭 분석
                match_result = matcher.analyze_keyword_match(title, keyword)

                logger.debug(f"상품 {i + 1} 매칭 결과: {match_result['reason']}")

                if match_result["is_match"]:
                    # 매칭된 상품에 매칭 정보 추가
                    matched_product = product.copy()
                    matched_product["match_info"] = {
                        "match_type": match_result["match_type"],
                        "match_score": match_result["score"],
                        "match_reason": match_result["reason"],
                    }
                    matched_products.append(matched_product)
                    logger.info(
                        f"상품 {i + 1} 매칭 성공: title='{title[:30]}', type={match_result['match_type']}, score={match_result['score']:.3f}"
                    )

            # 매칭 스코어 기준으로 정렬 (높은 순)
            matched_products.sort(
                key=lambda x: x["match_info"]["match_score"], reverse=True
            )

            logger.success(
                f"키워드 매칭 완료: keyword='{keyword}', total_products={len(products)}, matched_products={len(matched_products)}"
            )

            if matched_products:
                best_match = matched_products[0]
                logger.info(
                    f"최고 매칭 상품: title='{best_match['title'][:30]}', score={best_match['match_info']['match_score']:.3f}"
                )

            return {
                "job_id": request.job_id,
                "schedule_id": request.schedule_id,
                "schedule_his_id": request.schedule_his_id,
                "keyword": keyword,
                "matched_products": matched_products,
                "status": "success",
            }

        except Exception as e:
            logger.error(
                f"매칭 서비스 오류: job_id={request.job_id}, keyword='{keyword}', error='{e}'"
            )
            raise InvalidItemDataException()
