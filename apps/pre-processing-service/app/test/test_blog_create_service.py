import unittest
from unittest.mock import patch, MagicMock

from app.service.blog.blog_create_service import BlogContentService
from app.model.schemas import RequestBlogCreate


class TestBlogContentGeneration(unittest.TestCase):
    """블로그 콘텐츠 생성 핵심 로직 테스트"""

    @patch.dict('os.environ', {'OPENAI_API_KEY': 'test-key'})
    @patch('app.service.blog.blog_create_service.OpenAI')
    def setUp(self, mock_openai_class):
        """테스트 설정 - OpenAI Mock 적용"""
        # Mock OpenAI 클라이언트 설정
        self.mock_client = MagicMock()
        mock_openai_class.return_value = self.mock_client

        # 서비스 인스턴스 생성
        self.service = BlogContentService()

    def test_generate_blog_content_success(self):
        """정상적인 콘텐츠 생성 테스트"""
        # Mock 응답 설정
        mock_choice = MagicMock()
        mock_choice.message.content = """<h1>아이폰 15 케이스 완벽 가이드</h1>
<h2>제품 소개</h2>
<p>이 케이스는 뛰어난 보호 성능을 제공합니다.</p>"""

        mock_response = MagicMock()
        mock_response.choices = [mock_choice]

        self.mock_client.chat.completions.create.return_value = mock_response

        # 테스트 요청
        request = RequestBlogCreate(
            keyword="아이폰 케이스",
            product_info={
                "title": "아이폰 15 투명 케이스",
                "price": 25000
            }
        )

        # 실행
        result = self.service.generate_blog_content(request)

        # 검증
        self.assertIn("title", result)
        self.assertIn("content", result)
        self.assertIn("tags", result)
        # 실제 파싱 로직에 따른 제목 검증 (키워드가 제목에 포함되지 않아 기본 제목 생성됨)
        self.assertEqual(result["title"], "아이폰 15 투명 케이스 - 아이폰 케이스 완벽 가이드")
        self.assertIn("<h1>", result["content"])
        self.assertIn("아이폰 케이스", result["tags"])

    def test_generate_blog_content_api_failure(self):
        """API 실패 시 폴백 콘텐츠 생성 테스트"""
        # API 실패 시뮬레이션
        self.mock_client.chat.completions.create.side_effect = Exception("API Error")

        request = RequestBlogCreate(keyword="테스트 키워드")

        # 실행
        result = self.service.generate_blog_content(request)

        # 폴백 콘텐츠 검증
        self.assertIn("title", result)
        self.assertIn("content", result)
        self.assertIn("tags", result)
        self.assertEqual(result["title"], "테스트 키워드 - 완벽 가이드")

    def test_generate_blog_content_minimal_input(self):
        """최소한의 입력으로 콘텐츠 생성 테스트"""
        # API 실패 시뮬레이션
        self.mock_client.chat.completions.create.side_effect = Exception("API Error")

        request = RequestBlogCreate()

        result = self.service.generate_blog_content(request)

        # 기본 콘텐츠 생성 확인
        self.assertEqual(result["title"], "상품 정보 및 구매 가이드")
        self.assertIn("<h1>", result["content"])
        self.assertEqual(result["tags"], ["상품정보", "리뷰"])