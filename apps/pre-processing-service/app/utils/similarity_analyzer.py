import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from loguru import logger
import onnxruntime as ort
from transformers import AutoTokenizer

class SimilarityAnalyzerONNX:
    """ONNX 기반 텍스트 유사도 분석기"""

    def __init__(self, model_path: str = "klue_bert.onnx"):
        try:
            logger.info("토크나이저 로딩 중: klue/bert-base")
            self.tokenizer = AutoTokenizer.from_pretrained("klue/bert-base")
            logger.info(f"ONNX 모델 로딩 중: {model_path}")
            self.ort_session = ort.InferenceSession(model_path)
            logger.success("ONNX 모델 로딩 성공")
        except Exception as e:
            logger.error(f"모델 로딩 실패: {e}")
            raise e

    def get_embedding(self, text: str) -> np.ndarray:
        """텍스트 임베딩 생성 (ONNX)"""
        try:
            logger.debug(f"임베딩 생성 시작: text='{text[:50]}'")
            # 토큰화
            inputs = self.tokenizer(
                text, return_tensors="np", padding=True, truncation=True, max_length=128
            )
            ort_inputs = {
                "input_ids": inputs["input_ids"].astype(np.int64),
                "attention_mask": inputs["attention_mask"].astype(np.int64)
            }
            ort_outs = self.ort_session.run(None, ort_inputs)
            embedding = ort_outs[0][:, 0, :]  # [CLS] 토큰 임베딩
            logger.debug(f"임베딩 생성 완료: shape={embedding.shape}")
            return embedding
        except Exception as e:
            logger.error(f"임베딩 생성 오류: text='{text[:30]}', error='{e}'")
            raise

    def calculate_similarity(self, text1: str, text2: str) -> float:
        """두 텍스트 간 유사도 계산"""
        try:
            logger.debug(f"유사도 계산 시작: text1='{text1[:30]}', text2='{text2[:30]}'")
            emb1 = self.get_embedding(text1)
            emb2 = self.get_embedding(text2)
            similarity = cosine_similarity(emb1, emb2)[0][0]
            logger.debug(f"유사도 계산 완료: similarity={similarity:.4f}")
            return similarity
        except Exception as e:
            logger.error(f"유사도 계산 오류: {e}")
            raise

    def analyze_similarity_batch(self, keyword: str, product_titles: list[str]) -> list[dict]:
        """배치 유사도 분석"""
        logger.info(f"배치 유사도 분석 시작: keyword='{keyword}', titles_count={len(product_titles)}")
        try:
            keyword_emb = self.get_embedding(keyword)
            results = []

            for i, title in enumerate(product_titles):
                try:
                    title_emb = self.get_embedding(title)
                    sim = cosine_similarity(keyword_emb, title_emb)[0][0]
                    results.append({"index": i, "title": title, "similarity": float(sim), "score": float(sim)})
                except Exception as e:
                    logger.error(f"유사도 계산 오류 (제목: {title[:30]}): {e}")
                    results.append({"index": i, "title": title, "similarity": 0.0, "score": 0.0})

            results.sort(key=lambda x: x["similarity"], reverse=True)
            logger.info(f"배치 유사도 분석 완료: 총 {len(results)}개, 최고 유사도={results[0]['similarity']:.4f}")
            return results
        except Exception as e:
            logger.error(f"배치 유사도 분석 실패: {e}")
            raise
