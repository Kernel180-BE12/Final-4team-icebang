import os
import time
import pyperclip

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from selenium.common.exceptions import TimeoutException

from app.errors.CrawlingException import *
from app.errors.BlogPostingException import *
from app.service.blog.base_blog_post_service import BaseBlogPostService

class NaverBlogPostService(BaseBlogPostService):
    """네이버 블로그 포스팅 서비스 구현"""

    def _load_config(self) -> None:
        """네이버 블로그 설정 로드"""

        self.id = os.getenv("NAVER_ID", "all2641")
        self.password = os.getenv("NAVER_PASSWORD", "cjh83520*")
        self.login_url = "https://nid.naver.com/nidlogin.login"
        self.post_content_url = f"https://blog.naver.com/PostWriteForm.naver?blogId={self.id}&Redirect=Write&redirect=Write&widgetTypeCall=true&noTrackingCode=true&directAccess=false"

    def _get_platform_name(self) -> str:
        return "NAVER_BLOG"

    def _validate_content(self, title: str, content: str, tags: Optional[List[str]] = None) -> None:
        """공통 유효성 검사 로직"""

        if not title or not title.strip():
            raise BlogContentValidationException("title", "제목이 비어있습니다")

        if not content or not content.strip():
            raise BlogContentValidationException("content", "내용이 비어있습니다")

        if tags is None:
            raise BlogContentValidationException("tags", "태그가 비어있습니다")

    def _login(self) -> None:
        """네이버 로그인 구현"""

        try:
            self.web_driver.get(self.login_url)

            try:
                id_input = self.wait_driver.until(
                    EC.presence_of_element_located((By.ID, "id"))
                )
                time.sleep(2)
            except TimeoutException:
                raise ElementNotFoundException("id")

            pyperclip.copy(self.id)
            time.sleep(1)
            id_input.send_keys(Keys.COMMAND, 'v')
            time.sleep(1)

            # 비밀번호 입력
            try:
                password_input = self.wait_driver.until(
                    EC.presence_of_element_located((By.ID, "pw"))
                )
            except TimeoutException:
                raise ElementNotFoundException("pw")

            pyperclip.copy(self.password)
            time.sleep(1)
            password_input.send_keys(Keys.COMMAND, 'v')
            time.sleep(1)

            # 로그인 버튼 클릭
            try:
                login_button = self.wait_driver.until(
                    EC.element_to_be_clickable((By.ID, "log.login"))
                )
                login_button.click()
                time.sleep(3)
            except TimeoutException:
                raise ElementNotFoundException("log.login")

        except (ElementNotFoundException, BlogLoginException):
            raise
        except TimeoutException:
            raise PageLoadTimeoutException(self.login_url)
        except WebDriverConnectionException:
            raise BlogServiceUnavailableException("네이버 블로그", "네트워크 연결 오류 또는 페이지 로드 실패")
        except Exception as e:
            raise BlogLoginException("네이버 블로그", f"예상치 못한 오류: {str(e)}")

    def _write_content(self, title: str, content: str, tags: List[str] = None) -> None:
        """네이버 블로그 포스팅 작성 구현"""
        from selenium.webdriver.common.by import By
        from selenium.webdriver.support import expected_conditions as EC
        from selenium.webdriver.common.keys import Keys
        from selenium.webdriver.common.action_chains import ActionChains
        from selenium.common.exceptions import TimeoutException

        try:
            self.web_driver.get(self.post_content_url)

            # 기존 작성 글 팝업 닫기 (있을 경우)
            try:
                cancel = self.wait_driver.until(
                    EC.element_to_be_clickable((By.CSS_SELECTOR, '.se-popup-button.se-popup-button-cancel'))
                )
                cancel.click()
                time.sleep(1)
            except:
                pass

            # 제목 입력
            try:
                title_element = self.wait_driver.until(
                    EC.element_to_be_clickable((By.CSS_SELECTOR, '.se-placeholder.__se_placeholder.se-fs32'))
                )
                ActionChains(self.web_driver).move_to_element(title_element).click().pause(0.2).send_keys(
                    title).perform()
                time.sleep(1)
            except TimeoutException:
                raise BlogElementInteractionException("제목 입력 필드", "제목 입력")

            # 본문 입력
            try:
                body_element = self.wait_driver.until(
                    EC.element_to_be_clickable((By.CSS_SELECTOR, '.se-component.se-text.se-l-default'))
                )
                ActionChains(self.web_driver).move_to_element(body_element).click().pause(0.2) \
                    .send_keys(content).pause(0.2).send_keys(Keys.ENTER).perform()
                time.sleep(1)
            except TimeoutException:
                raise BlogElementInteractionException("본문 입력 필드", "본문 입력")

            # 발행 버튼 클릭
            try:
                publish_btn = self.wait_driver.until(
                    EC.element_to_be_clickable((By.XPATH, "//button[.//span[normalize-space()='발행']]"))
                )
                try:
                    publish_btn.click()
                except Exception:
                    self.web_driver.execute_script("arguments[0].click();", publish_btn)
                time.sleep(2)
            except TimeoutException:
                raise BlogElementInteractionException("발행 버튼", "버튼 클릭")

            # 태그 입력
            if tags:
                try:
                    tag_input = self.wait_driver.until(
                        EC.element_to_be_clickable((By.CSS_SELECTOR, "input[placeholder*='태그']"))
                    )
                    for tag in tags:
                        tag_input.send_keys(tag)
                        tag_input.send_keys(Keys.SPACE)
                        time.sleep(0.5)
                except TimeoutException:
                    raise BlogElementInteractionException("태그 입력 필드", "태그 입력")

            # 최종 발행 확인
            try:
                time.sleep(1)
                final_btn = self.wait_driver.until(
                    EC.element_to_be_clickable((By.XPATH,
                                                "//div[contains(@class,'layer') or contains(@class,'popup') or @role='dialog']//*[self::button or self::a][.//span[normalize-space()='발행']]"))
                )
                try:
                    final_btn.click()
                except Exception:
                    self.web_driver.execute_script("arguments[0].click();", final_btn)
            except TimeoutException:
                raise BlogElementInteractionException("최종 발행 버튼", "버튼 클릭")

            # 발행 완료 확인
            try:
                self.wait_driver.until(
                    EC.any_of(
                        EC.url_contains("PostView.naver"),
                        EC.url_contains("postList"),
                        EC.url_contains("postList.naver"),
                        EC.url_contains("entry.naver")
                    )
                )
            except TimeoutException:
                pass

        except (BlogElementInteractionException, BlogPostPublishException):
            raise
        except TimeoutException:
            raise PageLoadTimeoutException(self.post_content_url)
        except WebDriverConnectionException:
            raise BlogServiceUnavailableException("네이버 블로그", "페이지 로드 중 네트워크 오류")
        except Exception as e:
            raise BlogPostPublishException("네이버 블로그", f"예상치 못한 오류: {str(e)}")
