# import torch
# import numpy as np
# from sklearn.metrics.pairwise import cosine_similarity
# from transformers import AutoTokenizer, AutoModel
# from loguru import logger
#
#
# class SimilarityAnalyzer:
#     """텍스트 유사도 분석기"""
#
#     def __init__(self):
#         try:
#             logger.info("KLUE BERT 모델 로딩 시도 중...")
#             self.tokenizer = AutoTokenizer.from_pretrained("klue/bert-base")
#             self.model = AutoModel.from_pretrained("klue/bert-base")
#             logger.success("KLUE BERT 모델 로딩 성공")
#         except Exception as e:
#             logger.warning(f"KLUE BERT 로딩 실패, 다국어 BERT로 대체: {e}")
#             try:
#                 logger.info("다국어 BERT 모델 로딩 시도 중...")
#                 self.tokenizer = AutoTokenizer.from_pretrained(
#                     "bert-base-multilingual-cased"
#                 )
#                 self.model = AutoModel.from_pretrained("bert-base-multilingual-cased")
#                 logger.success("다국어 BERT 모델 로딩 성공")
#             except Exception as e2:
#                 logger.error(f"모든 BERT 모델 로딩 실패: {e2}")
#                 raise e2
#
#     def get_embedding(self, text: str) -> np.ndarray:
#         """텍스트 임베딩 생성"""
#         try:
#             logger.debug(f"임베딩 생성 시작: text='{text[:50]}'")
#             inputs = self.tokenizer(
#                 text, return_tensors="pt", padding=True, truncation=True, max_length=128
#             )
#             with torch.no_grad():
#                 outputs = self.model(**inputs)
#             embedding = outputs.last_hidden_state[:, 0, :].numpy()
#             logger.debug(f"임베딩 생성 완료: shape={embedding.shape}")
#             return embedding
#         except Exception as e:
#             logger.error(f"임베딩 생성 오류: text='{text[:30]}', error='{e}'")
#             raise
#
#     def calculate_similarity(self, text1: str, text2: str) -> float:
#         """두 텍스트 간 유사도 계산"""
#         try:
#             logger.debug(
#                 f"유사도 계산 시작: text1='{text1[:30]}', text2='{text2[:30]}'"
#             )
#             embedding1 = self.get_embedding(text1)
#             embedding2 = self.get_embedding(text2)
#             similarity = cosine_similarity(embedding1, embedding2)[0][0]
#             logger.debug(f"유사도 계산 완료: similarity={similarity:.4f}")
#             return similarity
#         except Exception as e:
#             logger.error(
#                 f"유사도 계산 오류: text1='{text1[:30]}', text2='{text2[:30]}', error='{e}'"
#             )
#             raise
#
#     def analyze_similarity_batch(
#         self, keyword: str, product_titles: list[str]
#     ) -> list[dict]:
#         """배치로 유사도 분석"""
#         logger.info(
#             f"배치 유사도 분석 시작: keyword='{keyword}', titles_count={len(product_titles)}"
#         )
#
#         try:
#             keyword_embedding = self.get_embedding(keyword)
#             results = []
#
#             for i, title in enumerate(product_titles):
#                 try:
#                     logger.debug(
#                         f"유사도 계산 중 ({i + 1}/{len(product_titles)}): title='{title[:30]}'"
#                     )
#                     title_embedding = self.get_embedding(title)
#                     similarity = cosine_similarity(keyword_embedding, title_embedding)[
#                         0
#                     ][0]
#
#                     results.append(
#                         {
#                             "index": i,
#                             "title": title,
#                             "similarity": float(similarity),
#                             "score": float(similarity),
#                         }
#                     )
#                     logger.debug(
#                         f"유사도 계산 완료 ({i + 1}/{len(product_titles)}): similarity={similarity:.4f}"
#                     )
#                 except Exception as e:
#                     logger.error(f"유사도 계산 오류 (제목: {title[:30]}): {e}")
#                     results.append(
#                         {"index": i, "title": title, "similarity": 0.0, "score": 0.0}
#                     )
#
#             # 유사도 기준 내림차순 정렬
#             results.sort(key=lambda x: x["similarity"], reverse=True)
#             logger.info(
#                 f"배치 유사도 분석 완료: 총 {len(results)}개, 최고 유사도={results[0]['similarity']:.4f}"
#             )
#             return results
#         except Exception as e:
#             logger.error(f"배치 유사도 분석 실패: keyword='{keyword}', error='{e}'")
#             raise
