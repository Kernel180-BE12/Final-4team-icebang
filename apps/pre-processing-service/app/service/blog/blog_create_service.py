import json
import logging
import os
from datetime import datetime
from typing import Dict, List, Optional, Any

from openai import OpenAI
from dotenv import load_dotenv

from app.model.schemas import RequestBlogCreate
from app.errors.BlogPostingException import *

# 환경변수 로드
load_dotenv(".env.dev")


class BlogContentService:
    """RAG를 사용한 블로그 콘텐츠 생성 전용 서비스"""

    def __init__(self):
        # OpenAI API 키 설정
        self.openai_api_key = os.getenv("OPENAI_API_KEY")
        if not self.openai_api_key:
            raise ValueError("OPENAI_API_KEY가 .env.dev 파일에 설정되지 않았습니다.")

        # 인스턴스 레벨에서 클라이언트 생성
        self.client = OpenAI(api_key=self.openai_api_key)
        logging.basicConfig(level=logging.INFO)
        self.logger = logging.getLogger(__name__)

    def generate_blog_content(self, request: RequestBlogCreate) -> Dict[str, Any]:
        """
        요청 데이터를 기반으로 블로그 콘텐츠 생성

        Args:
            request: RequestBlogCreate 객체

        Returns:
            Dict: {"title": str, "content": str, "tags": List[str]} 형태의 결과
        """
        try:
            # 1. 콘텐츠 정보 정리
            content_context = self._prepare_content_context(request)

            # 2. 프롬프트 생성
            prompt = self._create_content_prompt(content_context, request)

            # 3. GPT를 통한 콘텐츠 생성
            generated_content = self._generate_with_openai(prompt)

            # 4. 콘텐츠 파싱 및 구조화
            return self._parse_generated_content(generated_content, request)

        except Exception as e:
            self.logger.error(f"콘텐츠 생성 실패: {e}")
            return self._create_fallback_content(request)

    def _prepare_content_context(self, request: RequestBlogCreate) -> str:
        """요청 데이터를 콘텐츠 생성용 컨텍스트로 변환"""
        context_parts = []

        # 키워드 정보 추가
        if request.keyword:
            context_parts.append(f"주요 키워드: {request.keyword}")

        # 상품 정보 추가
        if request.product_info:
            context_parts.append("\n상품 정보:")

            # 상품 기본 정보
            if request.product_info.get("title"):
                context_parts.append(f"- 상품명: {request.product_info['title']}")

            if request.product_info.get("price"):
                context_parts.append(f"- 가격: {request.product_info['price']:,}원")

            if request.product_info.get("rating"):
                context_parts.append(f"- 평점: {request.product_info['rating']}/5.0")

            # 상품 상세 정보
            if request.product_info.get("description"):
                context_parts.append(f"- 설명: {request.product_info['description']}")

            # 상품 사양 (material_info 등)
            if request.product_info.get("material_info"):
                context_parts.append("- 주요 사양:")
                specs = request.product_info["material_info"]
                if isinstance(specs, dict):
                    for key, value in specs.items():
                        context_parts.append(f"  * {key}: {value}")

            # 상품 옵션
            if request.product_info.get("options"):
                options = request.product_info["options"]
                context_parts.append(f"- 구매 옵션 ({len(options)}개):")
                for i, option in enumerate(options[:5], 1):  # 최대 5개만
                    if isinstance(option, dict):
                        option_name = option.get("name", f"옵션 {i}")
                        context_parts.append(f"  {i}. {option_name}")
                    else:
                        context_parts.append(f"  {i}. {option}")

            # 구매 링크
            if request.product_info.get("url") or request.product_info.get(
                "product_url"
            ):
                url = request.product_info.get("url") or request.product_info.get(
                    "product_url"
                )
                context_parts.append(f"- 구매 링크: {url}")

        return "\n".join(context_parts) if context_parts else "키워드 기반 콘텐츠 생성"

    def _create_content_prompt(self, context: str, request: RequestBlogCreate) -> str:
        """콘텐츠 생성용 프롬프트 생성"""

        # 기본 키워드가 없으면 상품 제목에서 추출
        main_keyword = request.keyword
        if (
            not main_keyword
            and request.product_info
            and request.product_info.get("title")
        ):
            main_keyword = request.product_info["title"]

        prompt = f"""
다음 정보를 바탕으로 매력적인 블로그 포스트를 작성해주세요.

정보:
{context}

작성 가이드라인:
- 스타일: 친근하면서도 신뢰할 수 있는, 정보 제공 중심
- 길이: 1200자 내외의 적당한 길이
- 톤: 독자의 관심을 끄는 자연스러운 어조

작성 요구사항:
1. SEO 친화적이고 클릭하고 싶은 매력적인 제목
2. 독자의 관심을 끄는 도입부
3. 핵심 특징과 장점을 구체적으로 설명
4. 실제 사용 시나리오나 활용 팁
5. 구매 결정에 도움이 되는 정보

⚠️ 주의:
- 절대로 마지막에 'HTML 구조는…' 같은 자기 평가 문장을 추가하지 마세요.
- 출력 시 ```나 ```html 같은 코드 블록 구문을 포함하지 마세요.
- 오직 HTML 태그만 사용하여 구조화된 콘텐츠를 작성해주세요.
(예: <h2>, <h3>, <p>, <ul>, <li> 등)
"""

        return prompt

    def _generate_with_openai(self, prompt: str) -> str:
        """OpenAI API를 통한 콘텐츠 생성"""
        try:
            response = self.client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {
                        "role": "system",
                        "content": "당신은 전문적인 블로그 콘텐츠 작성자입니다. 상품 리뷰와 정보성 콘텐츠를 매력적이고 SEO 친화적으로 작성합니다.",
                    },
                    {"role": "user", "content": prompt},
                ],
                temperature=0.7,
                max_tokens=2000,
            )

            return response.choices[0].message.content

        except Exception as e:
            self.logger.error(f"OpenAI API 호출 실패: {e}")
            raise

    def _parse_generated_content(
        self, content: str, request: RequestBlogCreate
    ) -> Dict[str, Any]:
        """생성된 콘텐츠를 파싱하여 구조화"""

        # 제목 추출 (첫 번째 h1이나 강조된 줄)
        lines = content.strip().split("\n")
        title = "블로그 포스트"  # 기본값

        for line in lines[:10]:  # 처음 10줄에서 제목 찾기
            clean_line = (
                line.strip()
                .replace("#", "")
                .replace("<h1>", "")
                .replace("</h1>", "")
                .replace("<h2>", "")
                .replace("</h2>", "")
            )
            if clean_line and len(clean_line) > 5 and len(clean_line) < 100:
                title = clean_line
                break

        # 키워드가 있으면 제목에 없을 경우 기본 제목 생성
        if request.keyword and request.keyword not in title:
            if request.product_info and request.product_info.get("title"):
                title = (
                    f"{request.product_info['title']} - {request.keyword} 완벽 가이드"
                )
            else:
                title = f"{request.keyword} - 완벽 가이드"

        # 태그 생성
        tags = self._generate_tags(request)

        return {"title": title, "content": content, "tags": tags}

    def _generate_tags(self, request: RequestBlogCreate) -> List[str]:
        """요청 정보 기반 태그 생성"""
        tags = []

        # 키워드 추가
        if request.keyword:
            tags.append(request.keyword)

        # 상품 정보에서 태그 추출
        if request.product_info:
            # 상품명에서 키워드 추출
            if request.product_info.get("title"):
                title = request.product_info["title"].lower()

                # 일반적인 제품 카테고리 태그
                if any(word in title for word in ["iphone", "아이폰", "phone"]):
                    tags.extend(["아이폰", "스마트폰"])
                if any(word in title for word in ["필름", "보호", "강화"]):
                    tags.extend(["보호필름", "강화필름"])
                if any(word in title for word in ["케이스", "커버"]):
                    tags.extend(["폰케이스", "액세서리"])
                if any(word in title for word in ["노트북", "laptop"]):
                    tags.extend(["노트북", "컴퓨터"])
                if any(word in title for word in ["마우스", "키보드"]):
                    tags.extend(["컴퓨터용품", "PC액세서리"])

            # 재료/사양 정보에서 태그 생성
            if request.product_info.get("material_info"):
                material_info = request.product_info["material_info"]
                if isinstance(material_info, dict):
                    for key, value in material_info.items():
                        if value and len(str(value).strip()) <= 20:
                            clean_value = str(value).strip()
                            if clean_value not in tags:
                                tags.append(clean_value)

        # 기본 태그 추가
        if not tags:
            tags = ["상품정보", "리뷰"]

        # 중복 제거 및 개수 제한
        unique_tags = []
        for tag in tags:
            if tag not in unique_tags and len(unique_tags) < 10:
                unique_tags.append(tag)

        return unique_tags

    def _create_fallback_content(self, request: RequestBlogCreate) -> Dict[str, Any]:
        """콘텐츠 생성 실패 시 대안 콘텐츠 생성"""

        if request.product_info and request.product_info.get("title"):
            title = f"{request.product_info['title']} - 상품 정보 및 구매 가이드"
            product_name = request.product_info["title"]
        elif request.keyword:
            title = f"{request.keyword} - 완벽 가이드"
            product_name = request.keyword
        else:
            title = "상품 정보 및 구매 가이드"
            product_name = "상품"

        content = f"""
<h1>{title}</h1>

<h2>상품 소개</h2>
<p>{product_name}에 대한 상세한 정보를 소개합니다.</p>

<h2>주요 특징</h2>
<ul>
<li>고품질의 제품으로 신뢰할 수 있는 브랜드입니다</li>
<li>합리적인 가격으로 가성비가 뛰어납니다</li>
<li>사용자 친화적인 디자인과 기능을 제공합니다</li>
</ul>
"""

        if request.product_info:
            if request.product_info.get("price"):
                content += f"<h2>가격 정보</h2>\n<p>판매가: <strong>{request.product_info['price']:,}원</strong></p>\n"

            if request.product_info.get("material_info"):
                content += "<h2>상품 사양</h2>\n<ul>\n"
                for key, value in request.product_info["material_info"].items():
                    content += f"<li><strong>{key}:</strong> {value}</li>\n"
                content += "</ul>\n"

        content += """
<h2>구매 안내</h2>
<p>신중한 검토를 통해 만족스러운 구매 결정을 내리시기 바랍니다.</p>
"""

        return {
            "title": title,
            "content": content,
            "tags": self._generate_tags(request),
        }


# if __name__ == '__main__':
#     # 테스트용 요청 데이터
#     test_request = RequestBlogCreate(
#         keyword="아이폰 케이스",
#         product_info={
#             "title": "아이폰 15 프로 투명 케이스",
#             "price": 29900,
#             "rating": 4.8,
#             "description": "9H 강화 보호 기능을 제공하는 투명 케이스",
#             "material_info": {
#                 "소재": "TPU + PC",
#                 "두께": "1.2mm",
#                 "색상": "투명",
#                 "호환성": "아이폰 15 Pro"
#             },
#             "options": [
#                 {"name": "투명"},
#                 {"name": "반투명"},
#                 {"name": "블랙"}
#             ],
#             "url": "https://example.com/iphone-case"
#         }
#     )
#
#     # 서비스 실행
#     service = BlogContentService()
#     print("=== 블로그 콘텐츠 생성 테스트 ===")
#     print(f"키워드: {test_request.keyword}")
#     print(f"상품: {test_request.product_info['title']}")
#     print("\n--- 생성 시작 ---")
#
#     result = service.generate_blog_content(test_request)
#
#     print(f"\n=== 생성 결과 ===")
#     print(f"제목: {result['title']}")
#     print(f"\n태그: {', '.join(result['tags'])}")
#     print(f"\n내용:\n{result['content']}")
#     print(f"\n글자수: {len(result['content'])}자")
