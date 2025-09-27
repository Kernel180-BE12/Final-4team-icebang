import logging
import os
import boto3
from loguru import logger
from datetime import datetime
from typing import Dict, List, Optional, Any

from openai import OpenAI
from app.core.config import settings
from app.model.schemas import RequestBlogCreate
from app.errors.BlogPostingException import *


class BlogContentService:
    """RAG를 사용한 블로그 콘텐츠 생성 전용 서비스"""

    def __init__(self):
        # OpenAI API 키 설정
        self.openai_api_key = settings.OPENAI_API_KEY
        if not self.openai_api_key:
            raise ValueError("OPENAI_API_KEY가 .env.dev 파일에 설정되지 않았습니다.")

        self.client = OpenAI(api_key=self.openai_api_key)

        # S3 클라이언트 추가
        self.s3_client = boto3.client(
            "s3",
            aws_access_key_id=os.getenv("AWS_ACCESS_KEY_ID"),
            aws_secret_access_key=os.getenv("AWS_SECRET_ACCESS_KEY"),
            region_name=os.getenv("AWS_REGION", "ap-northeast-2")
        )
        self.bucket_name = os.getenv("S3_BUCKET_NAME", "icebang4-dev-bucket")

        logging.basicConfig(level=logging.INFO)

    def _fetch_images_from_s3(self, keyword: str, product_index: int = 1) -> List[Dict]:
        """S3에서 해당 상품의 이미지 정보를 조회"""
        try:
            # 폴더 패턴: 20250922_키워드_1/ 형식으로 검색
            from datetime import datetime
            date_str = datetime.now().strftime("%Y%m%d")

            # 키워드 정리 (S3UploadUtil과 동일한 방식)
            safe_keyword = (
                keyword.replace("/", "-")
                .replace("\\", "-")
                .replace(" ", "_")
                .replace(":", "-")
                .replace("*", "-")
                .replace("?", "-")
                .replace('"', "-")
                .replace("<", "-")
                .replace(">", "-")
                .replace("|", "-")[:20]
            )

            folder_prefix = f"product/{date_str}_{safe_keyword}_{product_index}/"

            logger.debug(f"S3에서 이미지 조회: {folder_prefix}")

            # S3에서 해당 폴더의 파일 목록 조회
            response = self.s3_client.list_objects_v2(
                Bucket=self.bucket_name,
                Prefix=folder_prefix
            )

            if 'Contents' not in response:
                logger.warning(f"S3에서 이미지를 찾을 수 없음: {folder_prefix}")
                return []

            images = []
            base_url = f"https://{self.bucket_name}.s3.ap-northeast-2.amazonaws.com"

            # 이미지 파일만 필터링 (image_*.jpg 패턴)
            for obj in response['Contents']:
                key = obj['Key']
                file_name = key.split('/')[-1]  # 마지막 부분이 파일명

                # 이미지 파일인지 확인
                if file_name.startswith('image_') and file_name.endswith(('.jpg', '.jpeg', '.png')):
                    # 파일 크기 정보 (bytes -> KB)
                    file_size_kb = obj['Size'] / 1024

                    # 인덱스 추출 (image_001.jpg -> 1)
                    try:
                        index = int(file_name.split('_')[1].split('.')[0])
                    except:
                        index = len(images) + 1

                    images.append({
                        "index": index,
                        "s3_url": f"{base_url}/{key}",
                        "file_name": file_name,
                        "file_size_kb": round(file_size_kb, 2),
                        "original_url": ""  # 원본 URL은 S3에서 조회 불가
                    })

            # 인덱스 순으로 정렬
            images.sort(key=lambda x: x['index'])

            logger.success(f"S3에서 이미지 {len(images)}개 조회 완료")
            return images

        except Exception as e:
            logger.error(f"S3 이미지 조회 실패: {e}")
            return []

    def generate_blog_content(self, request: RequestBlogCreate) -> Dict[str, Any]:
        """
        요청 데이터를 기반으로 블로그 콘텐츠 생성 (이미지 자동 배치 포함)
        """
        try:
            logger.debug("[STEP1] 콘텐츠 컨텍스트 준비 시작")
            content_context = self._prepare_content_context(request)
            logger.debug(f"[STEP1 완료] context length={len(content_context)}")

            logger.debug("[STEP2] 프롬프트 생성 시작")
            prompt = self._create_content_prompt(content_context, request)
            logger.debug(f"[STEP2 완료] prompt length={len(prompt)}")

            logger.debug("[STEP3] OpenAI API 호출 시작")
            generated_content = self._generate_with_openai(prompt)
            logger.debug(f"[STEP3 완료] generated length={len(generated_content)}")

            logger.debug("[STEP4] 콘텐츠 파싱 시작")
            result = self._parse_generated_content(generated_content, request)
            logger.debug("[STEP4 완료]")

            # STEP5: S3에서 이미지 정보 조회 (새로 추가)
            uploaded_images = request.uploaded_images
            if not uploaded_images and request.keyword:
                logger.debug("[STEP5-1] S3에서 이미지 정보 조회 시작")
                uploaded_images = self._fetch_images_from_s3(request.keyword)
                logger.debug(f"[STEP5-1 완료] 조회된 이미지: {len(uploaded_images)}개")

            # STEP6: 이미지 자동 배치
            if uploaded_images and len(uploaded_images) > 0:
                logger.debug("[STEP6] 이미지 자동 배치 시작")
                result['content'] = self._insert_images_to_content(
                    result['content'],
                    uploaded_images
                )
                logger.debug("[STEP6 완료] 이미지 배치 완료")

            return result

        except Exception as e:
            logger.error(f"콘텐츠 생성 실패: {e}")
            return self._create_fallback_content(request)

    def _prepare_content_context(self, request: RequestBlogCreate) -> str:
        """요청 데이터를 콘텐츠 생성용 컨텍스트로 변환"""
        context_parts = []

        # 키워드 정보
        if request.keyword:
            context_parts.append(f"주요 키워드: {request.keyword}")

        # 상품 정보
        if request.product_info:
            context_parts.append("\n상품 정보:")

            if request.product_info.get("title"):
                context_parts.append(f"- 상품명: {request.product_info['title']}")

            if request.product_info.get("price"):
                try:
                    context_parts.append(f"- 가격: {int(request.product_info['price']):,}원")
                except Exception:
                    context_parts.append(f"- 가격: {request.product_info.get('price')}")

            if request.product_info.get("rating"):
                context_parts.append(f"- 평점: {request.product_info['rating']}/5.0")

            if request.product_info.get("description"):
                context_parts.append(f"- 설명: {request.product_info['description']}")

            if request.product_info.get("material_info"):
                context_parts.append("- 주요 사양:")
                specs = request.product_info["material_info"]
                if isinstance(specs, dict):
                    for key, value in specs.items():
                        context_parts.append(f"  * {key}: {value}")

            if request.product_info.get("options"):
                options = request.product_info["options"]
                context_parts.append(f"- 구매 옵션 ({len(options)}개):")
                for i, option in enumerate(options[:5], 1):
                    if isinstance(option, dict):
                        option_name = option.get("name", f"옵션 {i}")
                        context_parts.append(f"  {i}. {option_name}")
                    else:
                        context_parts.append(f"  {i}. {option}")

            if request.product_info.get("url") or request.product_info.get("product_url"):
                url = request.product_info.get("url") or request.product_info.get("product_url")
                context_parts.append(f"- 구매 링크: {url}")

        # 번역 텍스트 (translation_language) 추가
        if request.translation_language:
            context_parts.append("\n이미지(OCR)에서 추출·번역된 텍스트:")
            context_parts.append(request.translation_language.strip())

        return "\n".join(context_parts) if context_parts else "키워드 기반 콘텐츠 생성"

    def _select_best_images(self, uploaded_images: List[Dict], target_count: int = 4) -> List[Dict]:
        """크기 기반으로 최적의 이미지 4개 선별"""
        if not uploaded_images:
            return []

        logger.debug(f"이미지 선별 시작: 전체 {len(uploaded_images)}개 -> 목표 {target_count}개")

        # 1단계: 너무 작은 이미지 제외 (20KB 이하는 아이콘, 로고 가능성)
        filtered = [img for img in uploaded_images if img.get('file_size_kb', 0) > 20]
        logger.debug(f"크기 필터링 후: {len(filtered)}개 이미지 남음")

        if len(filtered) == 0:
            # 모든 이미지가 너무 작다면 원본에서 선택
            filtered = uploaded_images

        # 2단계: 크기순 정렬 (큰 이미지 = 메인 상품 사진일 가능성)
        sorted_images = sorted(filtered, key=lambda x: x.get('file_size_kb', 0), reverse=True)

        # 3단계: 상위 이미지 선택하되, 너무 많으면 균등 분산
        if len(sorted_images) <= target_count:
            selected = sorted_images
        else:
            # 상위 2개 (메인 이미지) + 나머지에서 균등분산으로 2개
            selected = sorted_images[:2]  # 큰 이미지 2개

            remaining = sorted_images[2:]
            if len(remaining) >= 2:
                step = len(remaining) // 2
                selected.extend([remaining[i * step] for i in range(2)])

        result = selected[:target_count]

        logger.debug(f"최종 선택된 이미지: {len(result)}개")
        for i, img in enumerate(result):
            logger.debug(f"  {i + 1}. {img.get('file_name', 'unknown')} ({img.get('file_size_kb', 0):.1f}KB)")

        return result

    def _insert_images_to_content(self, content: str, uploaded_images: List[Dict]) -> str:
        """AI가 적절한 위치에 이미지 4개를 자동 배치"""

        # 1단계: 최적의 이미지 4개 선별
        selected_images = self._select_best_images(uploaded_images, target_count=4)

        if not selected_images:
            logger.warning("선별된 이미지가 없어서 이미지 배치를 건너뜀")
            return content

        logger.debug(f"이미지 배치 시작: {len(selected_images)}개 이미지")

        # 2단계: AI에게 이미지 배치 위치 물어보기
        image_placement_prompt = f"""
다음 HTML 콘텐츠에서 이미지 {len(selected_images)}개를 적절한 위치에 배치해주세요.

콘텐츠:
{content}

이미지 개수: {len(selected_images)}개

요구사항:
- 각 섹션(h2, h3 태그)마다 골고루 분산 배치
- 너무 몰려있지 않게 적절한 간격 유지
- 글의 흐름을 방해하지 않는 자연스러운 위치
- [IMAGE_1], [IMAGE_2], [IMAGE_3], [IMAGE_4] 형식의 플레이스홀더로 표시

⚠️ 주의사항:
- 기존 HTML 구조와 내용은 그대로 유지
- 오직 이미지 플레이스홀더만 적절한 위치에 삽입
- 코드 블록(```)은 사용하지 말고 수정된 HTML만 반환

수정된 HTML을 반환해주세요.
"""

        try:
            # 3단계: AI로 배치 위치 결정
            modified_content = self._generate_with_openai(image_placement_prompt)

            # 4단계: 플레이스홀더를 실제 img 태그로 교체
            for i, img in enumerate(selected_images):
                img_tag = f'''
<div style="text-align: center; margin: 20px 0;">
    <img src="{img["s3_url"]}" alt="상품 이미지 {i + 1}" 
         style="max-width: 100%; height: auto; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
</div>'''

                placeholder = f"[IMAGE_{i + 1}]"
                modified_content = modified_content.replace(placeholder, img_tag)

            # 5단계: 남은 플레이스홀더 제거 (혹시 AI가 더 많이 만들었을 경우)
            import re
            modified_content = re.sub(r'\[IMAGE_\d+\]', '', modified_content)

            logger.success(f"이미지 배치 완료: {len(selected_images)}개 이미지 삽입")
            return modified_content

        except Exception as e:
            logger.error(f"이미지 배치 중 오류: {e}, 원본 콘텐츠 반환")
            return content

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
3. 핵심 특징과 장점을 구체적으로 설명 (h2, h3 태그로 구조화)
4. 실제 사용 시나리오나 활용 팁
5. 구매 결정에 도움이 되는 정보

⚠️ 주의:
- 절대로 마지막에 'HTML 구조는…' 같은 자기 평가 문장을 추가하지 마세요.
- 출력 시 ```나 ```html 같은 코드 블록 구문을 포함하지 마세요.
- 오직 HTML 태그만 사용하여 구조화된 콘텐츠를 작성해주세요.
(예: <h2>, <h3>, <p>, <ul>, <li> 등)
- 이미지는 나중에 자동으로 삽입되므로 img 태그를 작성하지 마세요.
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
            logger.error(f"OpenAI API 호출 실패: {e}")
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