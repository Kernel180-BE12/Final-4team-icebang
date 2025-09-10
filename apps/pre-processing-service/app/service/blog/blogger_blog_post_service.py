import json
import os
import pickle
from typing import Dict, List, Optional

from googleapiclient.discovery import build
from google.auth.transport.requests import Request
from google_auth_oauthlib.flow import InstalledAppFlow

from app.errors.BlogPostingException import *
from app.service.blog.base_blog_post_service import BaseBlogPostService


class BloggerBlogPostService(BaseBlogPostService):
    """
    Blogger API를 사용하여 포스팅을 관리하는 서비스
    """

    def __init__(self, config_file="blog_config.json"):
        # 부모 클래스 생성자 호출 (WebDriver는 None으로 설정됨)
        super().__init__()

        # API 관련 추가 초기화
        self.config_file = config_file
        self.blogger_service = None
        self.blog_id = None
        self.scopes = ["https://www.googleapis.com/auth/blogger"]

    def _requires_webdriver(self) -> bool:
        """API 기반 서비스는 WebDriver가 필요하지 않음"""
        return False

    def _load_config(self) -> None:
        """
        플랫폼별 설정 로드
        """
        try:
            with open(self.config_file, "r", encoding="utf-8") as f:
                self.config = json.load(f)
                self.current_upload_account = self.config["upload_account"]
        except FileNotFoundError:
            default_config = {
                "upload_account": "your_account@gmail.com",
                "credentials": "credentials.json",
            }
            with open(self.config_file, "w", encoding="utf-8") as f:
                json.dump(default_config, f, indent=2)
            self.config = default_config
            self.current_upload_account = self.config["upload_account"]

    def _login(self) -> None:
        """
        API 인증 (Selenium의 로그인을 대체)
        """
        self._authenticate_api()

    def _authenticate_api(self):
        """
        API 인증 및 서비스 객체 생성
        """
        token_file = f"token_{self.current_upload_account.replace('@', '_').replace('.', '_')}.pkl"

        try:
            creds = None
            if os.path.exists(token_file):
                with open(token_file, "rb") as token:
                    creds = pickle.load(token)

            if not creds or not creds.valid:
                if creds and creds.expired and creds.refresh_token:
                    creds.refresh(Request())
                else:
                    print(f"새 API 인증이 필요합니다: {self.current_upload_account}")
                    flow = InstalledAppFlow.from_client_secrets_file(
                        self.config["credentials"], self.scopes
                    )
                    creds = flow.run_local_server(port=0)

                with open(token_file, "wb") as token:
                    pickle.dump(creds, token)

            self.blogger_service = build("blogger", "v3", credentials=creds)

            blogs = self.blogger_service.blogs().listByUser(userId="self").execute()
            if blogs.get("items"):
                self.blog_id = blogs["items"][0]["id"]
                print(f"API 설정 완료 - 블로그: {blogs['items'][0]['name']}")
                return True
            else:
                print("블로그를 찾을 수 없습니다.")
                return False
        except Exception as e:
            print(f"API 인증/설정 실패: {e}")
            raise BloggerApiException("API 인증 실패", e)

    def _write_content(self, title: str, content: str, tags: List[str] = None) -> None:
        """
        API를 사용하여 포스팅 작성
        """
        if not self.blogger_service or not self.blog_id:
            self._authenticate_api()

        post_data = {"title": title, "content": content, "labels": tags or []}

        try:
            result = (
                self.blogger_service.posts()
                .insert(blogId=self.blog_id, body=post_data)
                .execute()
            )

            print(f"포스트 생성 완료: {result.get('url')}")
        except Exception as e:
            raise BlogPostPublishException(
                platform="Blogger", reason="API 통신 중 오류가 발생했습니다."
            ) from e

    def _get_platform_name(self) -> str:
        """플랫폼 이름 반환"""
        return "Blogger"

    def _validate_content(
        self, title: str, content: str, tags: Optional[List[str]] = None
    ) -> None:
        """
        공통 유효성 검사 로직
        """
        if not title or not title.strip():
            raise BlogContentValidationException("title", "제목이 비어있습니다")

        if not content or not content.strip():
            raise BlogContentValidationException("content", "내용이 비어있습니다")

        # 태그 유효성 검사도 필요에 따라 추가
        # if not tags or not isinstance(tags, list):
        #     raise BlogContentValidationException("tags", "태그는 리스트 형태여야 합니다")

    def __del__(self):
        """
        리소스 정리 - API 기반 서비스는 별도 정리 불필요
        부모 클래스의 __del__이 WebDriver 정리를 처리
        """
        super().__del__()
