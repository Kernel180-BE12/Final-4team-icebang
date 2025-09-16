import pytest
from app.service.product_blog_posting_service import (
    ProductBlogPostingService,
    BlogContentRequest,
    ProductData,
)

# 샘플 데이터
sample_product_data = {
    "tag": "test001",
    "product_url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=902500949447",
    "status": "success",
    "product_detail": {
        "url": "https://ssadagu.kr/shop/view.php?platform=1688&num_iid=902500949447",
        "title": "코닝 적용 가능한 애플 13 강화 필름 iphone16/15promax 휴대 전화 필름 애플 11 안티-peep 및 먼지없는 빈",
        "price": 430,
        "rating": 5.0,
        "options": [
            {"name": "먼지 없는 창고 2차 필름 [코닝글라스 방폭丨초투명]", "stock": 0},
            {
                "name": "먼지 없는 창고 2차 필름 [코닝글라스 방폭丨훔쳐보기 방지]",
                "stock": 0,
            },
        ],
        "material_info": {
            "상표": "다른",
            "재료": "강화 유리",
            "필름 종류": "전막",
            "크기": "애플 16프로맥스( 6.9inch )",
            "적용 모델": "iPhone13 Pro Max",
        },
        "product_images": [],
    },
    "crawled_at": "2025-09-16 11:49:24",
}


@pytest.fixture
def blog_service():
    return ProductBlogPostingService()


def test_generate_blog_content(blog_service):
    """GPT를 통한 블로그 콘텐츠 생성 테스트"""
    request = BlogContentRequest(
        content_style="informative",
        target_keywords=["아이폰", "강화필름", "보호필름", "스마트폰액세서리"],
        include_pricing=True,
        content_length="medium",
    )

    product_obj = ProductData.from_dict(sample_product_data)

    # 순수 콘텐츠 생성만 테스트
    blog_content = blog_service.content_generator.generate_blog_content(
        product_obj, request
    )

    assert blog_content.title
    assert "<h2>" in blog_content.content
    assert len(blog_content.tags) > 0


def test_post_product_to_blogger(blog_service, monkeypatch):
    """Blogger 포스팅 테스트 (실제 API 호출을 막고 mock)"""

    class MockBloggerAdapter:
        def post_content(self, title, content, tags):
            return {"mock": True}

    monkeypatch.setattr(blog_service, "blogger_service", MockBloggerAdapter())

    request = BlogContentRequest(
        content_style="informative",
        target_keywords=["아이폰", "강화필름", "보호필름", "스마트폰액세서리"],
        include_pricing=True,
        content_length="medium",
    )

    product_obj = ProductData.from_dict(sample_product_data)

    result = blog_service.post_product_to_blogger(product_obj, request)

    assert result["status"] == "success"
    assert result["platform"] == "blogger"
    assert "title" in result
    assert "tags" in result
