import os
import time

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException

from app.errors.CrawlingException import *
from app.errors.BlogPostingException import *
from app.service.blog.base_blog_post_service import BaseBlogPostService


class TistoryBlogPostService(BaseBlogPostService):
    """티스토리 블로그 포스팅 서비스"""

    def __init__(self, blog_id: str, blog_password: str, blog_name:str ,use_webdriver=True):
        """네이버 블로그 서비스 초기화

        Args:
            blog_id: 네이버 아이디
            blog_password: 네이버 비밀번호
            use_webdriver: 웹드라이버 사용 여부
        """
        self.blog_id = blog_id
        self.blog_password = blog_password
        self.blog_name = blog_name
        super().__init__(use_webdriver)

    def _load_config(self) -> None:
        """티스토리 블로그 설정 로드"""

        self.blog_name = self.blog_name
        self.id = self.blog_id
        self.password = self.blog_password
        self.login_url = "https://accounts.kakao.com/login/?continue=https%3A%2F%2Fkauth.kakao.com%2Foauth%2Fauthorize%3Fclient_id%3D3e6ddd834b023f24221217e370daed18%26state%3DaHR0cHM6Ly93d3cudGlzdG9yeS5jb20v%26redirect_uri%3Dhttps%253A%252F%252Fwww.tistory.com%252Fauth%252Fkakao%252Fredirect%26response_type%3Dcode%26auth_tran_id%3Dslj3F.mFC~2JNOiCOGi5HdGPKOA.Pce4l5tiS~3fZkInLGuEG3tMq~xZkxx4%26ka%3Dsdk%252F2.7.3%2520os%252Fjavascript%2520sdk_type%252Fjavascript%2520lang%252Fko-KR%2520device%252FMacIntel%2520origin%252Fhttps%25253A%25252F%25252Fwww.tistory.com%26is_popup%3Dfalse%26through_account%3Dtrue&talk_login=hidden#login"
        self.post_content_url = f"https://{self.blog_name}.tistory.com/manage/newpost"

    def _get_platform_name(self) -> str:
        return "TISTORY_BLOG"

    def _validate_content(
        self, title: str, content: str, tags: Optional[List[str]] = None
    ) -> None:
        """공통 유효성 검사 로직"""

        if not title or not title.strip():
            raise BlogContentValidationException("title", "제목이 비어있습니다")

        if not content or not content.strip():
            raise BlogContentValidationException("content", "내용이 비어있습니다")

        if tags is None:
            raise BlogContentValidationException("tags", "태그가 비어있습니다")

    def _login(self) -> None:
        """티스토리 로그인 구현"""

        try:
            self.web_driver.get(self.login_url)

            # 아이디 입력
            try:
                id_input = self.wait_driver.until(
                    EC.presence_of_element_located((By.ID, "loginId--1"))
                )
            except TimeoutException:
                raise ElementNotFoundException("loginId--1")

            id_input.clear()
            id_input.send_keys(self.id)
            time.sleep(1)

            # 비밀번호 입력
            try:
                password_input = self.wait_driver.until(
                    EC.presence_of_element_located((By.ID, "password--2"))
                )
            except TimeoutException:
                raise ElementNotFoundException("password--2")

            password_input.clear()
            password_input.send_keys(self.password)
            time.sleep(1)

            # 로그인 버튼 클릭
            try:
                login_button = self.wait_driver.until(
                    EC.element_to_be_clickable((By.XPATH, "//*[text()='로그인']"))
                )
                login_button.click()
                time.sleep(2)
            except TimeoutException:
                raise ElementNotFoundException("로그인 버튼")

        except (ElementNotFoundException, BlogLoginException):
            raise
        except TimeoutException:
            raise PageLoadTimeoutException(self.login_url)
        except WebDriverConnectionException:
            raise BlogServiceUnavailableException(
                "티스토리 블로그", "네트워크 연결 오류 또는 페이지 로드 실패"
            )
        except Exception as e:
            raise BlogLoginException("티스토리 블로그", f"예상치 못한 오류: {str(e)}")

    def _write_content(self, title: str, content: str, tags: List[str] = None) -> None:
        """티스토리 블로그 포스팅 작성 구현"""

        try:
            self.web_driver.get(self.post_content_url)
            time.sleep(3)

            # 제목 입력
            try:
                title_input = self.wait_driver.until(
                    EC.presence_of_element_located((By.TAG_NAME, "textarea"))
                )
                title_input.clear()
                title_input.send_keys(title)
                time.sleep(1)
            except TimeoutException:
                raise BlogElementInteractionException("제목 입력 필드", "제목 입력")

            # 내용 입력
            try:
                iframe = self.wait_driver.until(
                    EC.presence_of_element_located(
                        (
                            By.XPATH,
                            "//iframe[contains(@title, 'Rich Text Area') or contains(@id, 'editor')]",
                        )
                    )
                )
                self.web_driver.switch_to.frame(iframe)

                body = self.wait_driver.until(
                    EC.presence_of_element_located((By.TAG_NAME, "body"))
                )
                body.clear()
                body.send_keys(content)

                self.web_driver.switch_to.default_content()

            except Exception:
                try:
                    # 일반 textarea나 div 에디터 찾기
                    content_selectors = [
                        "//div[@contenteditable='true']",
                        "//textarea[contains(@class, 'editor')]",
                        "//div[contains(@class, 'editor')]",
                    ]

                    content_area = None
                    for selector in content_selectors:
                        try:
                            content_area = self.web_driver.find_element(
                                By.XPATH, selector
                            )
                            break
                        except:
                            continue

                    if content_area:
                        content_area.clear()
                        content_area.send_keys(content)
                    else:
                        raise BlogElementInteractionException(
                            "본문 입력 필드", "본문 입력"
                        )

                except Exception:
                    raise BlogElementInteractionException("본문 입력 필드", "본문 입력")

            # 태그 입력
            if tags and len(tags) > 0:
                try:
                    tag_input = self.wait_driver.until(
                        EC.presence_of_element_located(
                            (
                                By.XPATH,
                                "//input[@placeholder='태그입력' or contains(@placeholder, '태그')]",
                            )
                        )
                    )
                    tag_input.clear()

                    for i, tag in enumerate(tags):
                        tag_input.send_keys(tag)
                        if i < len(tags) - 1:
                            tag_input.send_keys(",")
                        time.sleep(0.5)

                except Exception:
                    raise BlogElementInteractionException("태그 입력 필드", "태그 입력")

            # 완료 버튼 클릭
            try:
                complete_button = self.wait_driver.until(
                    EC.element_to_be_clickable((By.XPATH, "//*[text()='완료']"))
                )
                complete_button.click()
                time.sleep(3)
            except TimeoutException:
                raise BlogElementInteractionException("완료 버튼", "버튼 클릭")

            # 발행 설정
            try:
                public_option = self.wait_driver.until(
                    EC.element_to_be_clickable((By.XPATH, "//*[text()='공개']"))
                )
                public_option.click()
                time.sleep(1)

                publish_button = self.wait_driver.until(
                    EC.element_to_be_clickable((By.XPATH, "//*[text()='공개 발행']"))
                )
                publish_button.click()
                time.sleep(3)

            except Exception:
                try:
                    publish_selectors = [
                        "//button[contains(text(), '발행')]",
                        "//button[contains(text(), '저장')]",
                        "//*[@class='btn_publish' or contains(@class, 'publish')]",
                    ]

                    for selector in publish_selectors:
                        try:
                            publish_btn = self.web_driver.find_element(
                                By.XPATH, selector
                            )
                            publish_btn.click()
                            break
                        except:
                            continue
                    else:
                        raise BlogPostPublishException(
                            "티스토리 블로그", "발행 버튼을 찾을 수 없습니다"
                        )

                except Exception:
                    raise BlogPostPublishException(
                        "티스토리 블로그", "발행 과정에서 오류가 발생했습니다"
                    )

        except (BlogElementInteractionException, BlogPostPublishException):
            raise
        except TimeoutException:
            raise PageLoadTimeoutException(self.post_content_url)
        except WebDriverConnectionException:
            raise BlogServiceUnavailableException(
                "티스토리 블로그", "페이지 로드 중 네트워크 오류"
            )
        except Exception as e:
            raise BlogPostPublishException(
                "티스토리 블로그", f"예상치 못한 오류: {str(e)}"
            )
