import json
import os
import pickle
from typing import Dict, List, Optional
from googleapiclient.discovery import build
from google.auth.transport.requests import Request
from google_auth_oauthlib.flow import InstalledAppFlow
from app.errors.CrawlingException import *
from app.errors.BlogPostingException import *
from app.service.blog.adapters.api_blog_adapter import ApiBlogAdapter


class BloggerBlogAdapter(ApiBlogAdapter):
    """Blogger API 어댑터"""

    def __init__(self, config_file="blog_config.json"):
        super().__init__()
        self.config_file = config_file
        self.blogger_service = None
        self.blog_id = None
        self.scopes = ["https://www.googleapis.com/auth/blogger"]

    def _load_config(self) -> None:
        """Blogger 설정 로드"""
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

    def _setup_api_client(self) -> None:
        """API 클라이언트 설정"""
        # 설정 로드 후 추가 초기화가 필요하면 여기서 수행
        pass

    def get_platform_name(self) -> str:
        return "Blogger"

    def authenticate(self) -> None:
        """API 인증"""
        if self.authenticated:
            return

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
            self.api_client = self.blogger_service

            blogs = self.blogger_service.blogs().listByUser(userId="self").execute()
            if blogs.get("items"):
                self.blog_id = blogs["items"][0]["id"]
                print(f"API 설정 완료 - 블로그: {blogs['items'][0]['name']}")
                self.authenticated = True
            else:
                raise BloggerApiException("블로그를 찾을 수 없습니다")

        except Exception as e:
            raise BloggerApiException("API 인증 실패", e)

    def write_content(self, title: str, content: str, tags: List[str] = None) -> None:
        """API를 사용하여 포스팅 작성"""
        if not self.authenticated:
            self.authenticate()

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

    def validate_content(self, title: str, content: str, tags: Optional[List[str]] = None) -> None:
        """Blogger 유효성 검사"""
        super().validate_content(title, content, tags)
        # Blogger 특화 검증 로직 추가 가능