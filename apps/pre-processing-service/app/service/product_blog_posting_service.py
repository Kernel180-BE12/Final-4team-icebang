import json
import logging
import os
from datetime import datetime
from typing import Dict, List, Optional, Any
from dataclasses import dataclass
from enum import Enum

from openai import OpenAI
from dotenv import load_dotenv

from app.service.blog.blogger_blog_post_adapter import BloggerBlogPostAdapter
from app.errors.BlogPostingException import *

# 환경변수 로드
load_dotenv('.env.dev')

client = OpenAI()

class PostingStatus(Enum):
    PENDING = "pending"
    PROCESSING = "processing"
    SUCCESS = "success"
    FAILED = "failed"
    RETRY = "retry"


@dataclass
class ProductData:
    """크롤링된 상품 데이터 모델"""
    tag: str
    product_url: str
    title: str
    price: int
    rating: float
    options: List[Dict[str, Any]]
    material_info: Dict[str, str]
    product_images: List[str]
    crawled_at: str

    @classmethod
    def from_dict(cls, data: Dict) -> 'ProductData':
        """딕셔너리에서 ProductData 객체 생성"""
        product_detail = data.get('product_detail', {})
        return cls(
            tag=data.get('tag', ''),
            product_url=product_detail.get('url', ''),
            title=product_detail.get('title', ''),
            price=product_detail.get('price', 0),
            rating=product_detail.get('rating', 0.0),
            options=product_detail.get('options', []),
            material_info=product_detail.get('material_info', {}),
            product_images=product_detail.get('product_images', []),
            crawled_at=data.get('crawled_at', '')
        )


@dataclass
class BlogPostContent:
    """생성된 블로그 포스트 콘텐츠"""
    title: str
    content: str
    tags: List[str]


@dataclass
class BlogContentRequest:
    """블로그 콘텐츠 생성 요청"""
    content_style: str = "informative"  # "informative", "promotional", "review"
    target_keywords: List[str] = None
    include_pricing: bool = True
    include_specifications: bool = True
    content_length: str = "medium"  # "short", "medium", "long"


class ProductContentGenerator:
    """GPT를 활용한 상품 블로그 콘텐츠 생성"""

    def __init__(self):
        # 환경변수에서 OpenAI API 키 로드
        self.openai_api_key = os.getenv('OPENAI_API_KEY')
        if not self.openai_api_key:
            raise ValueError("OPENAI_API_KEY가 .env.dev 파일에 설정되지 않았습니다.")

        client.api_key = self.openai_api_key

    def generate_blog_content(self, product_data: ProductData, request: BlogContentRequest) -> BlogPostContent:
        """상품 데이터를 기반으로 블로그 콘텐츠 생성"""

        # 1. 상품 정보 정리
        product_info = self._format_product_info(product_data, request)

        # 2. 프롬프트 생성
        prompt = self._create_blog_prompt(product_info, request)

        # 3. GPT를 통한 콘텐츠 생성
        try:

            response = client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {
                        "role": "system",
                        "content": "당신은 전문적인 블로그 콘텐츠 작성자입니다. 상품 리뷰와 정보성 콘텐츠를 매력적이고 SEO 친화적으로 작성합니다."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                temperature=0.7,
                max_tokens=2000
            )

            generated_content = response.choices[0].message.content

            # 4. 콘텐츠 파싱 및 구조화
            return self._parse_generated_content(generated_content, product_data, request)

        except Exception as e:
            logging.error(f"콘텐츠 생성 실패: {e}")
            return self._create_fallback_content(product_data, request)

    def _format_product_info(self, product_data: ProductData, request: BlogContentRequest) -> str:
        """상품 정보를 텍스트로 포맷팅"""
        info_parts = [
            f"상품명: {product_data.title}",
        ]

        # 가격 정보 추가
        if request.include_pricing and product_data.price:
            info_parts.append(f"가격: {product_data.price:,}원")

        # 평점 정보 추가
        if product_data.rating:
            info_parts.append(f"평점: {product_data.rating}/5.0")

        # 사양 정보 추가
        if request.include_specifications and product_data.material_info:
            info_parts.append("\n상품 사양:")
            for key, value in product_data.material_info.items():
                info_parts.append(f"- {key}: {value}")

        # 옵션 정보 추가
        if product_data.options:
            info_parts.append(f"\n구매 옵션 ({len(product_data.options)}개):")
            for i, option in enumerate(product_data.options[:5], 1):  # 처음 5개만
                info_parts.append(f"{i}. {option.get('name', 'N/A')}")

        # 구매 링크
        if product_data.product_url:
            info_parts.append(f"\n구매 링크: {product_data.product_url}")

        return "\n".join(info_parts)

    def _create_blog_prompt(self, product_info: str, request: BlogContentRequest) -> str:
        """블로그 작성용 프롬프트 생성"""

        # 스타일별 가이드라인
        style_guidelines = {
            "informative": "객관적이고 상세한 정보 제공 중심으로, 독자가 제품을 이해할 수 있도록 전문적으로 작성",
            "promotional": "제품의 장점과 매력을 강조하며, 구매 의욕을 자극할 수 있도록 매력적으로 작성",
            "review": "실제 사용 경험을 바탕으로 한 솔직한 평가와 추천 중심으로 작성"
        }

        # 길이별 가이드라인
        length_guidelines = {
            "short": "800자 내외의 간결한 내용",
            "medium": "1200자 내외의 적당한 길이",
            "long": "1500자 이상의 상세한 내용"
        }

        style_guide = style_guidelines.get(request.content_style, style_guidelines["informative"])
        length_guide = length_guidelines.get(request.content_length, length_guidelines["medium"])

        # 키워드 정보
        keywords_text = ""
        if request.target_keywords:
            keywords_text = f"\n포함할 키워드: {', '.join(request.target_keywords)}"

        prompt = f"""
다음 상품 정보를 바탕으로 매력적인 블로그 포스트를 작성해주세요.

상품 정보:
{product_info}

작성 가이드라인:
- 스타일: {style_guide}
- 길이: {length_guide}
- 톤: 친근하면서도 신뢰할 수 있는, 정보 제공 중심{keywords_text}

작성 요구사항:
1. SEO 친화적이고 클릭하고 싶은 매력적인 제목
2. 독자의 관심을 끄는 도입부
3. 상품의 핵심 특징과 장점을 구체적으로 설명
4. 실제 사용 시나리오나 활용 팁
5. 구매 결정에 도움이 되는 정보

⚠️ 주의:
- 절대로 마지막에 '이 HTML 구조는…' 같은 자기 평가 문장을 추가하지 마세요.
- 출력 시 ```나 ```html 같은 코드 블록 구문을 포함하지 마세요.
- 오직 HTML 태그만 사용하여 구조화된 콘텐츠를 작성해주세요.
(예: <h2>, <h3>, <p>, <ul>, <li> 등)
"""

        return prompt

    def _parse_generated_content(self, content: str, product_data: ProductData,
                                 request: BlogContentRequest) -> BlogPostContent:
        """생성된 콘텐츠를 파싱하여 구조화"""

        # 제목 추출 (첫 번째 h1이나 강조된 줄)
        lines = content.strip().split('\n')
        title = product_data.title  # 기본값

        for line in lines[:10]:  # 처음 10줄에서 제목 찾기
            clean_line = line.strip().replace('#', '').replace('<h1>', '').replace('</h1>', '')
            if clean_line and len(clean_line) > 5 and ('제목' in line or '<h1>' in line or line.startswith('#')):
                title = clean_line
                break
            elif clean_line and len(clean_line) > 10 and len(clean_line) < 100:
                # 적당한 길이의 첫 번째 줄을 제목으로
                title = clean_line
                break

        # 태그 생성
        tags = self._generate_tags_from_product(product_data, request)

        return BlogPostContent(
            title=title,
            content=content,
            tags=tags
        )

    def _generate_tags_from_product(self, product_data: ProductData, request: BlogContentRequest) -> List[str]:
        """상품 정보 기반 태그 생성"""
        tags = []

        # 사용자 지정 키워드가 있으면 우선 추가
        if request.target_keywords:
            tags.extend(request.target_keywords[:5])

        # 기본 태그 추가
        if product_data.tag:
            tags.append(product_data.tag)

        # 제품 타입 추론해서 태그 추가
        title_lower = product_data.title.lower()
        if any(word in title_lower for word in ["iphone", "아이폰", "phone"]):
            tags.extend(["아이폰", "스마트폰"])
        if any(word in title_lower for word in ["필름", "보호", "강화"]):
            tags.extend(["보호필름", "강화필름"])
        if any(word in title_lower for word in ["케이스", "커버"]):
            tags.extend(["폰케이스", "액세서리"])

        # 재료 정보에서 태그 생성
        if product_data.material_info:
            for key, value in product_data.material_info.items():
                if value and len(value.strip()) <= 20:  # 너무 긴 값은 제외
                    clean_value = value.strip()
                    if clean_value not in tags:
                        tags.append(clean_value)

        # 중복 제거 및 개수 제한
        unique_tags = []
        for tag in tags:
            if tag not in unique_tags and len(unique_tags) < 10:
                unique_tags.append(tag)

        return unique_tags

    def _create_fallback_content(self, product_data: ProductData, request: BlogContentRequest) -> BlogPostContent:
        """콘텐츠 생성 실패 시 대안 콘텐츠 생성"""
        title = f"{product_data.title} - 상품 정보 및 구매 가이드"

        content = f"""
<h1>{product_data.title}</h1>

<h2>상품 소개</h2>
<p>{product_data.title}에 대한 상세한 정보를 소개합니다.</p>

<h2>가격 정보</h2>
<p>판매가: <strong>{product_data.price:,}원</strong></p>
"""

        if product_data.material_info:
            content += "<h2>상품 사양</h2>\n<ul>\n"
            for key, value in product_data.material_info.items():
                content += f"<li><strong>{key}:</strong> {value}</li>\n"
            content += "</ul>\n"

        if product_data.options:
            content += f"<h2>구매 옵션 ({len(product_data.options)}가지)</h2>\n<ul>\n"
            for option in product_data.options[:5]:
                content += f"<li>{option.get('name', 'N/A')}</li>\n"
            content += "</ul>\n"

        content += f"""
<h2>구매 안내</h2>
<p>상품 구매는 <a href="{product_data.product_url}" target="_blank">여기</a>에서 가능합니다.</p>
"""

        return BlogPostContent(
            title=title,
            content=content,
            tags=[product_data.tag] if product_data.tag else ["상품정보"]
        )

class ProductBlogPostingService:
    """상품 데이터를 Blogger에 포스팅하는 메인 서비스"""

    def __init__(self):
        self.content_generator = ProductContentGenerator()
        self.blogger_service = BloggerBlogPostAdapter()

    def post_product_to_blogger(self, product_data: ProductData, request: BlogContentRequest) -> dict:
        """상품 데이터를 Blogger에 포스팅"""
        try:
            # 1. GPT를 통한 콘텐츠 생성
            blog_content = self.content_generator.generate_blog_content(product_data, request)

            # 2. Blogger에 포스팅
            self.blogger_service.post_content(
                title=blog_content.title,
                content=blog_content.content,
                tags=blog_content.tags
            )

            # 3. 성공 결과 반환
            return {
                "status": "success",
                "platform": "blogger",
                "title": blog_content.title,
                "tags": blog_content.tags,
                "posted_at": datetime.now().isoformat(),
                "product_tag": product_data.tag
            }

        except Exception as e:
            logging.error(f"Blogger 포스팅 실패: {e}")
            # ProductData 객체 기준으로 처리
            return {
                "status": "failed",
                "error": str(e),
                "platform": "blogger",
                "attempted_at": datetime.now().isoformat(),
                "product_tag": getattr(product_data, "tag", "unknown")
            }

    # def batch_post_products(self, products_data: List[Dict], request: BlogContentRequest) -> List[Dict[str, Any]]:
    #     """여러 상품을 일괄 포스팅"""
    #     results = []
    #
    #     for product_data in products_data:
    #         result = self.post_product_to_blogger(product_data, request)
    #         results.append(result)
    #
    #         # API 호출 제한을 고려한 딜레이
    #         import time
    #         time.sleep(3)  # 3초 대기
    #
    #     return results