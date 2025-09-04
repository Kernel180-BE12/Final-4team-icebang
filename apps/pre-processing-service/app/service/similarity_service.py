from app.utils.similarity_analyzer import SimilarityAnalyzer
from app.errors.CustomException import InvalidItemDataException
from ..model.schemas import RequestSadaguSimilarity


def select_product_by_similarity(request: RequestSadaguSimilarity) -> dict:
    """
    BERT 기반 유사도 분석 후 상품 선택 - 4단계
    """
    keyword = request.keyword
    candidates = request.matched_products
    fallback_products = request.search_results or []

    # 매칭된 상품이 없으면 전체 검색 결과로 폴백
    if not candidates:
        if not fallback_products:
            return {
                "job_id": request.job_id,
                "schedule_id": request.schedule_id,
                "sschdule_his_id": request.sschdule_his_id,
                "keyword": keyword,
                "selected_product": None,
                "reason": "매칭된 상품과 검색 결과가 모두 없음",
                "status": "success"
            }

        print("매칭된 상품 없음 → 전체 검색 결과에서 유사도 분석")
        candidates = fallback_products
        analysis_mode = "fallback_similarity_only"
    else:
        analysis_mode = "matched_products"

    try:
        analyzer = SimilarityAnalyzer()

        print(f"키워드 '{keyword}'와 {len(candidates)}개 상품의 유사도 분석 시작... (모드: {analysis_mode})")

        # 한 개만 있으면 바로 선택
        if len(candidates) == 1:
            selected_product = candidates[0]

            # 유사도 계산
            similarity = analyzer.calculate_similarity(keyword, selected_product['title'])

            # 폴백 모드에서는 임계값 검증
            if analysis_mode == "fallback_similarity_only":
                similarity_threshold = 0.3
                if similarity < similarity_threshold:
                    return {
                        "job_id": request.job_id,
                        "schedule_id": request.schedule_id,
                        "sschdule_his_id": request.sschdule_his_id,
                        "keyword": keyword,
                        "selected_product": None,
                        "reason": f"단일 상품 유사도({similarity:.4f}) < 기준({similarity_threshold})",
                        "status": "success"
                    }

            selected_product['similarity_info'] = {
                'similarity_score': float(similarity),
                'analysis_type': 'single_candidate',
                'analysis_mode': analysis_mode
            }

            return {
                "job_id": request.job_id,
                "schedule_id": request.schedule_id,
                "sschdule_his_id": request.sschdule_his_id,
                "keyword": keyword,
                "selected_product": selected_product,
                "reason": f"단일 상품 - 유사도: {similarity:.4f} ({analysis_mode})",
                "status": "success"
            }

        # 여러 개가 있으면 유사도 비교
        print("여러 상품 중 최고 유사도로 선택...")

        # 제목만 추출해서 배치 분석
        titles = [product['title'] for product in candidates]
        similarity_results = analyzer.analyze_similarity_batch(keyword, titles)

        # 결과 출력
        for result in similarity_results:
            print(f"  {result['title'][:40]} | 유사도: {result['similarity']:.4f}")

        # 최고 유사도 선택
        best_result = similarity_results[0]
        selected_product = candidates[best_result['index']].copy()

        # 폴백 모드에서는 임계값 검증
        similarity_threshold = 0.3
        if analysis_mode == "fallback_similarity_only" and best_result['similarity'] < similarity_threshold:
            return {
                "job_id": request.job_id,
                "schedule_id": request.schedule_id,
                "sschdule_his_id": request.sschdule_his_id,
                "keyword": keyword,
                "selected_product": None,
                "reason": f"최고 유사도({best_result['similarity']:.4f}) < 기준({similarity_threshold})",
                "status": "success"
            }

        # 유사도 정보 추가
        selected_product['similarity_info'] = {
            'similarity_score': best_result['similarity'],
            'analysis_type': 'multi_candidate_bert',
            'analysis_mode': analysis_mode,
            'rank': 1,
            'total_candidates': len(candidates)
        }

        # 매칭 모드에서는 종합 점수도 계산
        if analysis_mode == "matched_products" and 'match_info' in selected_product:
            match_score = selected_product['match_info']['match_score']
            similarity_score = best_result['similarity']
            # 가중치: 매칭 40%, 유사도 60%
            final_score = match_score * 0.4 + similarity_score * 0.6
            selected_product['final_score'] = final_score
            reason = f"종합점수({final_score:.4f}) = 매칭({match_score:.4f})*0.4 + 유사도({similarity_score:.4f})*0.6"
        else:
            reason = f"유사도({best_result['similarity']:.4f}) 기준 선택 ({analysis_mode})"

        print(f"선택됨: {selected_product['title'][:50]} | {reason}")

        return {
            "job_id": request.job_id,
            "schedule_id": request.schedule_id,
            "sschdule_his_id": request.sschdule_his_id,
            "keyword": keyword,
            "selected_product": selected_product,
            "reason": reason,
            "status": "success"
        }

    except Exception as e:
        print(f"유사도 분석 서비스 오류: {e}")
        raise InvalidItemDataException(f"유사도 분석 실패: {str(e)}")