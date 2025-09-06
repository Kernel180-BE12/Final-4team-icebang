import os
import time

from app.service.crawling_service import CrawlingService
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains
import pyperclip
from typing import List

class NaverBlogPostService:
    """
    네이버 블로그 포스팅 서비스 클래스
    아직 구현되지 않음.
    추후에 전략 패턴을 통해 티스토리 블로그와 맞출 예정
    """

    def __init__(self):
        """
        네이버 블로그 포스팅 서비스 초기화
        1. CrawlingService 인스턴스 생성
        2. 셀레니움 웹 드라이버 및 대기 객체 설정
        3. 환경 변수에서 네이버 아이디 및 비밀번호 로드
        """

        self.crawling_service = CrawlingService()
        self.web_driver = self.crawling_service.get_driver()
        self.wait_driver = self.crawling_service.get_wait()

        self.id = os.getenv("NAVER_ID", "all2641")
        self.password = os.getenv("NAVER_PASSWORD", "cjh83520*")
        self.login_url = "https://nid.naver.com/nidlogin.login"
        self.post_content_url = f"https://blog.naver.com/PostWriteForm.naver?blogId={self.id}&Redirect=Write&redirect=Write&widgetTypeCall=true&noTrackingCode=true&directAccess=false"

    def _login(self) -> bool:
        """
        네이버 로그인 자동화 메서드
        :return: 로그인 성공 여부
        """

        try:
            print("네이버 로그인 페이지 접속 중...")
            self.web_driver.get(self.login_url)
            time.sleep(2)

            print("아이디 입력 중...")
            id_input = self.wait_driver.until(
                EC.presence_of_element_located((By.ID, "id"))
            )
            pyperclip.copy(self.id)
            time.sleep(1)
            id_input.send_keys(Keys.COMMAND, 'v')
            time.sleep(2)

            print("비밀번호 입력 중...")
            password_input = self.wait_driver.until(
                EC.presence_of_element_located((By.ID, "pw"))
            )
            pyperclip.copy(self.password)
            time.sleep(1)
            password_input.send_keys(Keys.COMMAND, 'v')
            time.sleep(2)

            print("로그인 버튼 클릭 중...")
            login_button = self.wait_driver.until(
                EC.element_to_be_clickable((By.ID, "log.login"))
            )
            login_button.click()
            time.sleep(3)

            return True

        except TimeoutException as e:
            print(f"요소를 찾는 데 시간이 초과되었습니다: {e}")
            return False

        except NoSuchElementException as e:
            print(f"요소를 찾을 수 없습니다: {e}")
            return False

        except Exception as e:
            print(f"오류가 발생했습니다: {e}")
            return False

    def _write_content(self, title: str, content: str, tags: List[str] = None) -> bool:
        """
        네이버 블로그 포스팅 자동화
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tags: 포스트 태그 리스트
        :return: 성공 여부
        """
        try:
            print("네이버 블로그 포스팅 페이지 접속 중...")
            self.web_driver.get(self.post_content_url)
            time.sleep(5)

            # 기존 작성 글 팝업 닫기 (있을 경우)
            try:
                cancel = self.wait_driver.until(
                    EC.element_to_be_clickable((By.CSS_SELECTOR, '.se-popup-button.se-popup-button-cancel'))
                )
                cancel.click()
                print("기존 작성 글 닫기")
                time.sleep(1)
            except:
                pass

            # 제목 입력
            print("제목 입력 중...")
            title_element = self.wait_driver.until(
                EC.element_to_be_clickable((By.CSS_SELECTOR, '.se-placeholder.__se_placeholder.se-fs32'))
            )
            ActionChains(self.web_driver).move_to_element(title_element).click().pause(0.2).send_keys(title).perform()
            print("제목 작성 완료")
            time.sleep(1)

            # 본문 입력
            print("본문 입력 중...")
            body_element = self.wait_driver.until(
                EC.element_to_be_clickable((By.CSS_SELECTOR, '.se-component.se-text.se-l-default'))
            )
            ActionChains(self.web_driver).move_to_element(body_element).click().pause(0.2) \
                .send_keys(content).pause(0.2).send_keys(Keys.ENTER).perform()
            print("내용 작성 완료")
            time.sleep(1)

            # 발행 버튼 클릭
            print("발행 버튼 찾는 중...")
            publish_btn = self.wait_driver.until(
                EC.element_to_be_clickable((By.XPATH, "//button[.//span[normalize-space()='발행']]"))
            )
            try:
                publish_btn.click()
            except Exception:
                self.web_driver.execute_script("arguments[0].click();", publish_btn)
            print("'발행' 버튼 클릭 완료")
            time.sleep(2)

            # 태그 입력 (발행 팝업에서)
            if tags:
                print("태그 입력 중...")
                try:
                    tag_input = self.wait_driver.until(
                        EC.element_to_be_clickable((By.CSS_SELECTOR, "input[placeholder*='태그']"))
                    )
                    for tag in tags:
                        tag_input.send_keys(tag)
                        tag_input.send_keys(Keys.SPACE)
                        time.sleep(0.5)
                    print("태그 입력 완료")
                except:
                    print("태그 입력 필드를 찾을 수 없음")

            # 최종 발행 확인
            time.sleep(1)
            final_btn = self.wait_driver.until(
                EC.element_to_be_clickable((By.XPATH,
                                            "//div[contains(@class,'layer') or contains(@class,'popup') or @role='dialog']//*[self::button or self::a][.//span[normalize-space()='발행']]"))
            )
            try:
                final_btn.click()
            except Exception:
                self.web_driver.execute_script("arguments[0].click();", final_btn)
            print("최종 발행 확인 버튼 클릭 완료")

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
                print("발행 완료!")
                return True
            except:
                print("발행 후 URL 변화 감지 실패했지만, 버튼 클릭은 완료")
                return True

        except Exception as e:
            print(f"글 작성 에러: {e}")
            return False

    def post_content(self,
                     title: str,
                     content: str,
                     tage: List[str] = None) -> bool:
        """
        블로그 포스트 작성
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tage: 포스트 태그 리스트
        :return: 포스팅 성공 여부
        """

        if self._login():
            print("네이버 로그인 성공")
        else:
            print("네이버 로그인 실패")
            raise Exception("네이버 로그인 실패")

        if self._write_content(title, content, tage):
            print("네이버 블로그 포스팅 성공")
        else:
            print("네이버 블로그 포스팅 실패")
            raise Exception("네이버 블로그 포스팅 실패")

        return True

    def __del__(self):
        """
        드라이버 종료
        """
        if self.web_driver:
            self.web_driver.quit()


if __name__ == "__main__":
    naver_service = NaverBlogPostService()
    naver_service.post_content("테스트 제목", "테스트 내용입니다.", ["태그1", "태그2"])
