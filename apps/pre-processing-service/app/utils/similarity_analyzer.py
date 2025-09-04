import torch
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from transformers import AutoTokenizer, AutoModel


class SimilarityAnalyzer:
    """텍스트 유사도 분석기"""

    def __init__(self):
        try:
            self.tokenizer = AutoTokenizer.from_pretrained('klue/bert-base')
            self.model = AutoModel.from_pretrained('klue/bert-base')
            print("KLUE BERT 모델 로딩 성공")
        except Exception as e:
            print(f"KLUE BERT 로딩 실패, 다국어 BERT로 대체: {e}")
            try:
                self.tokenizer = AutoTokenizer.from_pretrained('bert-base-multilingual-cased')
                self.model = AutoModel.from_pretrained('bert-base-multilingual-cased')
                print("다국어 BERT 모델 로딩 성공")
            except Exception as e2:
                print(f"모든 BERT 모델 로딩 실패: {e2}")
                raise e2

    def get_embedding(self, text: str) -> np.ndarray:
        """텍스트 임베딩 생성"""
        inputs = self.tokenizer(text, return_tensors='pt', padding=True, truncation=True, max_length=128)
        with torch.no_grad():
            outputs = self.model(**inputs)
        return outputs.last_hidden_state[:, 0, :].numpy()

    def calculate_similarity(self, text1: str, text2: str) -> float:
        """두 텍스트 간 유사도 계산"""
        embedding1 = self.get_embedding(text1)
        embedding2 = self.get_embedding(text2)
        return cosine_similarity(embedding1, embedding2)[0][0]

    def analyze_similarity_batch(self, keyword: str, product_titles: list[str]) -> list[dict]:
        """배치로 유사도 분석"""
        keyword_embedding = self.get_embedding(keyword)
        results = []

        for i, title in enumerate(product_titles):
            try:
                title_embedding = self.get_embedding(title)
                similarity = cosine_similarity(keyword_embedding, title_embedding)[0][0]

                results.append({
                    'index': i,
                    'title': title,
                    'similarity': float(similarity),
                    'score': float(similarity)
                })
            except Exception as e:
                print(f"유사도 계산 오류 (제목: {title[:30]}): {e}")
                results.append({
                    'index': i,
                    'title': title,
                    'similarity': 0.0,
                    'score': 0.0
                })

        # 유사도 기준 내림차순 정렬
        results.sort(key=lambda x: x['similarity'], reverse=True)
        return results