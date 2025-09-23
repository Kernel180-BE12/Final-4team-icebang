import json
import os
import pickle
from googleapiclient.discovery import build
from google.auth.transport.requests import Request
from google_auth_oauthlib.flow import InstalledAppFlow
from app.errors.BlogPostingException import *
from typing import Dict
from loguru import logger

class BloggerApiService:
    """
    호환되지 않는 Blogger API 서비스 (Adaptee)
    완전히 다른 초기화/인증 방식을 사용
    """

    def __init__(self, config_file:str):
        self.config_file = config_file
        self.config = {}
        self.current_upload_account = None
        self.blogger_service = None
        self.blog_id = None
        self.scopes = ["https://www.googleapis.com/auth/blogger"]
        self.authenticated = False

        self._load_api_config()

    def _load_api_config(self) -> None:
        """API 전용 설정 로드"""
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

    def authenticate_with_google_oauth(self) -> bool:
        """Google OAuth 인증 (Selenium 로그인과 완전히 다름)"""
        if self.authenticated:
            return True

        token_file = f"blogger/token_{self.current_upload_account.replace('@', '_').replace('.', '_')}.pkl"

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
                self.authenticated = True
                return True
            else:
                raise BloggerApiException("블로그를 찾을 수 없습니다")

        except Exception as e:
            raise BloggerApiException("API 인증 실패", e)

    def create_post_via_api(
        self, title: str, content: str, labels: List[str] = None
    ) -> Dict:
        """API를 통한 포스트 생성 (Selenium write_content와 완전히 다름)"""
        if not self.authenticated:
            self.authenticate_with_google_oauth()

        post_data = {"title": title, "content": content, "labels": labels or []}

        try:
            result = (
                self.blogger_service.posts()
                .insert(blogId=self.blog_id, body=post_data)
                .execute()
            )

            return {
                "blogger_post_id": result.get("id"),
                "published_url": result.get("url"),
                "status": "published",
            }
        except Exception as e:
            raise BlogPostPublishException(
                platform="Blogger", reason="API 통신 중 오류가 발생했습니다."
            ) from e

    def validate_api_content(
        self, title: str, content: str, labels: List[str] = None
    ) -> None:
        """API 전용 유효성 검사"""
        if not title or not title.strip():
            raise BlogContentValidationException("title", "제목이 비어있습니다")
        if not content or not content.strip():
            raise BlogContentValidationException("content", "내용이 비어있습니다")
        # Blogger는 태그가 선택사항
