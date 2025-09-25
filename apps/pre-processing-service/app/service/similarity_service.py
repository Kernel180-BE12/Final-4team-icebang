from app.utils.similarity_analyzer import SimilarityAnalyzerONNX
from app.errors.CustomException import InvalidItemDataException
from ..model.schemas import RequestSadaguSimilarity
from loguru import logger
from app.utils.response import Response


class SimilarityService:
    def __init__(self):
        pass

    def select_top_products_by_similarity(
        self, request: RequestSadaguSimilarity
    ) -> dict:
        """
        형태소 분석 후 Top 10 선택 (10개 이하면 유사도 분석 생략)
        """
        keyword = request.keyword
        candidates = request.matched_products
        fallback_products = request.search_results or []
        top_count = 10  # Top 10 개수 설정

        logger.info(
            f"상품 선택 서비스 시작 (Top {top_count}): keyword='{keyword}', matched_count={len(candidates) if candidates else 0}, fallback_count={len(fallback_products)}"
        )

        # 매칭된 상품이 없으면 전체 검색 결과로 폴백
        if not candidates:
            if not fallback_products:
                logger.warning(
                    f"매칭된 상품과 검색 결과가 모두 없음: keyword='{keyword}'"
                )

                data = {
                    "keyword": keyword,
                    "top_products": [],
                    "reason": "매칭된 상품과 검색 결과가 모두 없음",
                }
                return Response.ok(data, "매칭된 상품과 검색 결과가 모두 없습니다.")

            logger.info("매칭된 상품 없음 → 전체 검색 결과에서 유사도 분석 진행")
            candidates = fallback_products
            analysis_mode = "fallback_similarity_only"
            skip_similarity = False
        else:
            analysis_mode = "matched_products"
            # 형태소 분석 결과가 10개 이하면 유사도 분석 생략
            skip_similarity = len(candidates) <= top_count

        try:
            # 형태소 분석 결과가 10개 이하면 유사도 분석 생략하고 바로 반환
            if skip_similarity and analysis_mode == "matched_products":
                logger.info(
                    f"형태소 분석 결과가 {len(candidates)}개로 {top_count}개 이하 - 유사도 분석 생략"
                )

                # 매칭 스코어 기준으로 정렬된 상태 유지 (이미 match_service에서 정렬됨)
                top_products = []
                for i, product in enumerate(candidates):
                    enhanced_product = product.copy()
                    enhanced_product["rank"] = i + 1
                    enhanced_product["selection_info"] = {
                        "selection_type": "match_only",
                        "match_score": product.get("match_info", {}).get(
                            "match_score", 0.0
                        ),
                        "reason": "형태소 분석만으로 선택 (유사도 분석 생략)",
                        "total_candidates": len(candidates),
                    }
                    top_products.append(enhanced_product)

                logger.success(
                    f"형태소 분석만으로 상품 선택 완료: keyword='{keyword}', selected_count={len(top_products)}"
                )

                data = {
                    "keyword": keyword,
                    "top_products": top_products,
                    "reason": f"형태소 분석 결과 {len(candidates)}개 - 유사도 분석 생략",
                }
                return Response.ok(data)

            # 유사도 분석 필요한 경우 (매칭 결과가 10개 초과이거나 폴백 모드)
            analyzer = SimilarityAnalyzerONNX()

            logger.info(
                f"키워드 '{keyword}'와 {len(candidates)}개 상품의 유사도 분석 시작... (모드: {analysis_mode})"
            )

            # 모든 후보에 대해 유사도 계산
            titles = [product["title"] for product in candidates]
            similarity_results = analyzer.analyze_similarity_batch(keyword, titles)

            # 유사도 정보 추가 및 Top 10 선택
            enhanced_products = []
            similarity_threshold = (
                0.3 if analysis_mode == "fallback_similarity_only" else 0.0
            )

            for i, result in enumerate(similarity_results):
                product = candidates[result["index"]].copy()

                # 폴백 모드에서는 임계값 검증
                if (
                    analysis_mode == "fallback_similarity_only"
                    and result["similarity"] < similarity_threshold
                ):
                    logger.debug(
                        f"상품 {i + 1} 유사도 미달로 제외: similarity={result['similarity']:.4f} < threshold={similarity_threshold}"
                    )
                    continue

                product["similarity_info"] = {
                    "similarity_score": result["similarity"],
                    "analysis_type": "batch_similarity",
                    "analysis_mode": analysis_mode,
                }

                # 매칭 모드에서는 종합 점수 계산
                if analysis_mode == "matched_products" and "match_info" in product:
                    match_score = product["match_info"]["match_score"]
                    similarity_score = result["similarity"]
                    # 가중치: 매칭 40%, 유사도 60%
                    final_score = match_score * 0.4 + similarity_score * 0.6
                    product["final_score"] = final_score
                    product["selection_info"] = {
                        "selection_type": "match_and_similarity",
                        "match_score": match_score,
                        "similarity_score": similarity_score,
                        "final_score": final_score,
                        "reason": f"종합점수({final_score:.4f}) = 매칭({match_score:.4f})*0.4 + 유사도({similarity_score:.4f})*0.6",
                    }
                else:
                    product["selection_info"] = {
                        "selection_type": "similarity_only",
                        "similarity_score": result["similarity"],
                        "reason": f"유사도({result['similarity']:.4f}) 기준 선택 ({analysis_mode})",
                    }

                enhanced_products.append(product)

            # 종합 점수 또는 유사도 기준으로 재정렬
            if analysis_mode == "matched_products":
                enhanced_products.sort(
                    key=lambda x: x.get(
                        "final_score", x["similarity_info"]["similarity_score"]
                    ),
                    reverse=True,
                )
            else:
                enhanced_products.sort(
                    key=lambda x: x["similarity_info"]["similarity_score"], reverse=True
                )

            # Top 10 선택
            top_products = enhanced_products[:top_count]

            # 순위 정보 추가
            for i, product in enumerate(top_products):
                product["rank"] = i + 1

            logger.success(
                f"유사도 분석 완료: keyword='{keyword}', total_analyzed={len(candidates)}, valid_results={len(enhanced_products)}, top_selected={len(top_products)}"
            )

            if top_products:
                best_product = top_products[0]
                if "final_score" in best_product:
                    logger.info(
                        f"1위 상품: title='{best_product['title'][:30]}', final_score={best_product['final_score']:.4f}"
                    )
                else:
                    logger.info(
                        f"1위 상품: title='{best_product['title'][:30]}', similarity={best_product['similarity_info']['similarity_score']:.4f}"
                    )

            data = {
                "keyword": keyword,
                "top_products": top_products,
                "reason": f"유사도 분석 후 상위 {len(top_products)}개 선택 ({analysis_mode})",
            }
            return Response.ok(data)

        except Exception as e:
            logger.error(f"유사도 분석 서비스 오류: keyword='{keyword}', error='{e}'")
            raise InvalidItemDataException()
