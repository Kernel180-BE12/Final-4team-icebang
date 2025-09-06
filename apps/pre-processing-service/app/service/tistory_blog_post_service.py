import os
import time

from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from typing import List

from app.service.crawling_service import CrawlingService

class TistoryBlogPostService:

    """
    티스토리 블로그 포스팅 서비스 클래스
    추후에 전략 패턴을 통해 네이버 블로그와 맞출 예정
    """

    def __init__(self):
        """
        티스토리 블로그 포스팅 서비스 초기화
        1. CrawlingService 인스턴스 생성
        2. 셀레니움 웹 드라이버 및 대기 객체 설정
        3. 환경 변수에서 티스토리 아이디 및 비밀번호 로드
        """

        self.crawling_service = CrawlingService()
        self.web_driver = self.crawling_service.get_driver()
        self.wait_driver = self.crawling_service.get_wait()

        self.blog_name = os.getenv("TISTORY_BLOG_NAME", "hoons2641")
        self.id = os.getenv("TISTORY_ID", "fair_05@nate.com")
        self.password = os.getenv("TISTORY_PASSWORD", "kdyn264105*")
        self.login_url = "https://accounts.kakao.com/login/?continue=https%3A%2F%2Fkauth.kakao.com%2Foauth%2Fauthorize%3Fclient_id%3D3e6ddd834b023f24221217e370daed18%26state%3DaHR0cHM6Ly93d3cudGlzdG9yeS5jb20v%26redirect_uri%3Dhttps%253A%252F%252Fwww.tistory.com%252Fauth%252Fkakao%252Fredirect%26response_type%3Dcode%26auth_tran_id%3Dslj3F.mFC~2JNOiCOGi5HdGPKOA.Pce4l5tiS~3fZkInLGuEG3tMq~xZkxx4%26ka%3Dsdk%252F2.7.3%2520os%252Fjavascript%2520sdk_type%252Fjavascript%2520lang%252Fko-KR%2520device%252FMacIntel%2520origin%252Fhttps%25253A%25252F%25252Fwww.tistory.com%26is_popup%3Dfalse%26through_account%3Dtrue&talk_login=hidden#login"
        self.post_content_url = f"https://{self.blog_name}.tistory.com/manage/newpost"

    def _login(self) -> bool:
        """
        티스토리 로그인 자동화 메서드
        :return: 로그인 성공 여부
        """
        try:
            print("티스토리 홈페이지 접속 중...")
            self.web_driver.get(self.login_url)
            time.sleep(3)

            print("아이디 입력 중...")
            id_input = self.wait_driver.until(
                EC.presence_of_element_located((By.ID, "loginId--1"))
            )
            id_input.clear()
            id_input.send_keys(self.id)
            time.sleep(1)

            print("비밀번호 입력 중...")
            password_input = self.wait_driver.until(
                EC.presence_of_element_located((By.ID, "password--2"))
            )
            password_input.clear()
            password_input.send_keys(self.password)
            time.sleep(1)

            print("아이디와 비밀번호 입력 완료")

            print("로그인 버튼 찾는 중...")
            login_button = self.wait_driver.until(
                EC.element_to_be_clickable((By.XPATH, "//*[text()='로그인']"))
            )
            login_button.click()
            time.sleep(2)

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
        티스토리 블로그 포스팅 자동화
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tags: 포스트 태그 리스트
        :return: 성공 여부
        """
        print("글쓰기 페이지로 직접 이동 중...")
        self.web_driver.get(self.post_content_url)
        time.sleep(5)

        print("제목 입력 중...")
        try:
            title_input = self.wait_driver.until(
                EC.presence_of_element_located((By.TAG_NAME, "textarea"))
            )
            title_input.clear()
            title_input.send_keys(title)
            print("✅ 제목 입력 완료!")
        except Exception as e:
            print(f"❌ 제목 입력 실패: {e}")
            return False

        print("내용 입력 중...")
        try:
            iframe = self.wait_driver.until(
                EC.presence_of_element_located(
                    (By.XPATH, "//iframe[contains(@title, 'Rich Text Area') or contains(@id, 'editor')]"))
            )
            self.web_driver.switch_to.frame(iframe)

            body = self.wait_driver.until(
                EC.presence_of_element_located((By.TAG_NAME, "body"))
            )
            body.clear()
            body.send_keys(content)

            self.web_driver.switch_to.default_content()
            print("✅ 내용 입력 완료!")

        except Exception as e:
            print(f"⚠️ iframe 방식 실패, 다른 방법 시도: {e}")
            try:
                # 일반 textarea나 div 에디터 찾기
                content_selectors = [
                    "//div[@contenteditable='true']",
                    "//textarea[contains(@class, 'editor')]",
                    "//div[contains(@class, 'editor')]"
                ]

                content_area = None
                for selector in content_selectors:
                    try:
                        content_area = self.web_driver.find_element(By.XPATH, selector)
                        break
                    except:
                        continue

                if content_area:
                    content_area.clear()
                    content_area.send_keys(content)
                    print("✅ 내용 입력 완료!")
                else:
                    print("⚠️ 내용 입력란을 찾지 못했습니다.")

            except Exception as e2:
                print(f"⚠️ 내용 입력 실패: {e2}")

        if tags and len(tags) > 0:
            print(f"태그 입력 중: {tags}")
            try:
                tag_input = self.wait_driver.until(
                    EC.presence_of_element_located(
                        (By.XPATH, "//input[@placeholder='태그입력' or contains(@placeholder, '태그')]"))
                )
                tag_input.clear()

                for i, tag in enumerate(tags):
                    tag_input.send_keys(tag)
                    if i < len(tags) - 1:
                        tag_input.send_keys(",")
                    time.sleep(0.5)

                print("✅ 태그 입력 완료!")

            except Exception as e:
                print(f"⚠️ 태그 입력 실패: {e}")

            print("완료 버튼 클릭 중...")
            try:
                complete_button = self.wait_driver.until(
                    EC.element_to_be_clickable((By.XPATH, "//*[text()='완료']"))
                )
                complete_button.click()
                time.sleep(3)  # 팝업이 나타날 시간 대기
                print("✅ 완료 버튼 클릭 완료!")

            except Exception as e:
                print(f"❌ 완료 버튼 클릭 실패: {e}")
                return False

            print("발행 설정 중...")
            try:
                public_option = self.wait_driver.until(
                    EC.element_to_be_clickable((By.XPATH, "//*[text()='공개']"))
                )
                public_option.click()
                time.sleep(1)
                print("✅ 공개 설정 완료!")

                publish_button = self.wait_driver.until(
                    EC.element_to_be_clickable((By.XPATH, "//*[text()='공개 발행']"))
                )
                publish_button.click()
                time.sleep(3)
                print("✅ 공개 발행 완료!")

            except Exception as e:
                print(f"❌ 발행 설정 실패: {e}")
                try:
                    publish_selectors = [
                        "//button[contains(text(), '발행')]",
                        "//button[contains(text(), '저장')]",
                        "//*[@class='btn_publish' or contains(@class, 'publish')]"
                    ]

                    for selector in publish_selectors:
                        try:
                            publish_btn = self.web_driver.find_element(By.XPATH, selector)
                            publish_btn.click()
                            print("✅ 대체 발행 버튼 클릭 완료!")
                            break
                        except:
                            continue

                except Exception as e2:
                    print(f"❌ 대체 발행 방법도 실패: {e2}")
                    return False

            print("🎉 블로그 포스트 작성 및 발행 완료!")
            return True

        return True

    def post_content(self,
                     title: str,
                     content: str,
                     tags: List[str] = None):
        """
        블로그 포스트 작성
        :param title: 포스트 제목
        :param content: 포스트 내용
        :param tags: 포스트 태그 리스트
        :return: 포스팅 성공 여부
        """

        if not self._login():
            print("로그인 과정에서 오류가 발생하여 게시물 작성을 중단합니다.")
            return False

        if not self._write_content(title, content, tags):
            print("글 작성 과정에서 오류가 발생하여 게시물 작성을 중단합니다.")
            return False

        print("블로그 포스트 작성 완료")
        return True

    def __del__(self):
        """
        드라이버 종료
        """
        if self.web_driver:
            self.web_driver.quit()

if __name__ == "__main__":
    service = TistoryBlogPostService()
    service.post_content(
        title="테스트 제목",
        content="테스트 내용입니다.",
        tags=["테스트", "자동화", "티스토리"]
    )