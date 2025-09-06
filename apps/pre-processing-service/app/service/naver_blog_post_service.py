import os
import time

from app.service.crawling_service import CrawlingService
from app.errors.CrawlingException import *
from app.errors.BlogPostingException import *

from selenium.common.exceptions import TimeoutException, NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains
from typing import List, Optional
import pyperclip

class NaverBlogPostService:
    """
    네이버 블로그 포스팅 서비스 클래스
    login, write_content, post_content 메서드 포함
    1. _login: 네이버 로그인 자동화
    2. _write_content: 블로그 포스팅 작성 및 발행 자동화
    3. post_content: 로그인 및 포스팅 작성 통합 메서드
    4. __del__: 드라이버 종료
    """

    def __init__(self):
        """
        네이버 블로그 포스팅 서비스 초기화
        1. CrawlingService 인스턴스 생성
        2. 셀레니움 웹 드라이버 및 대기 객체 설정
        3. 환경 변수에서 아이디, 비밀번호, URL 로드
        """

        try:
            self.crawling_service = CrawlingService()
            self.web_driver = self.crawling_service.get_driver()
            self.wait_driver = self.crawling_service.get_wait()
        except Exception:
            raise WebDriverConnectionException()

        self.id = os.getenv("NAVER_ID", "all2641")
        self.password = os.getenv("NAVER_PASSWORD", "cjh83520*")
        self.login_url = "https://nid.naver.com/nidlogin.login"
        self.post_content_url = f"https://blog.naver.com/PostWriteForm.naver?blogId={self.id}&Redirect=Write&redirect=Write&widgetTypeCall=true&noTrackingCode=true&directAccess=false"

    def _validate_content(self, title: str, content: str, tags: Optional[List[str]] = None) -> None:
        """
        포스트 콘텐츠 기본 유효성 검사
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tags: 포스트 태그 리스트
        """
        if not title or not title.strip():
            raise BlogContentValidationException("title", "제목이 비어있습니다")

        if not content or not content.strip():
            raise BlogContentValidationException("content", "내용이 비어있습니다")

        if tags is None:
            raise BlogContentValidationException("tags", "태그가 비어있습니다")

    def _login(self):
        """
        네이버 로그인 자동화 메서드
        :return: 로그인 성공 여부
        """

        try:
            self.web_driver.get(self.login_url)
            # 아이디 입력
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

    def _write_content(self, title: str, content: str, tags: List[str] = None) -> bool:
        """
        네이버 블로그 포스팅 자동화
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tags: 포스트 태그 리스트
        :return: 성공 여부
        """
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

    def post_content(self,
                     title: str,
                     content: str,
                     tags: List[str] = None) -> dict:
        """
        블로그 포스트 작성
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tags: 포스트 태그 리스트
        :return: 발행 결과 딕셔너리
        """

        # 1. 콘텐츠 유효성 검사
        self._validate_content(title, content, tags)

        # 2. 로그인
        self._login()

        # 3. 포스트 작성 및 발행
        self._write_content(title, content, tags)

        return {
            "platform": "네이버 블로그",
            "title": title,
            "content_length": len(content),
            "tags": tags or []
        }

    def __del__(self):
        """
        드라이버 종료
        """
        if self.web_driver:
            self.web_driver.quit()
