import os
import time

from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from typing import List

from app.service.crawling_service import CrawlingService

class BlogPostService:

    def __init__(self):
        self.crawling_service = CrawlingService()
        self.driver = self.crawling_service.get_driver()
        self.wait = self.crawling_service.get_wait()
        self.id = os.getenv("BLOG_ID", "fair_05@nate.com")
        self.password = os.getenv("BLOG_PASSWORD", "kdyn264105*")

    def login_up_to_password_input(self, url: str) -> bool:
        """
        티스토리 카카오톡 로그인 과정 중 아이디와 비밀번호 입력까지 자동화합니다.
        로그인 버튼 클릭 직전까지 진행됩니다.
        """
        try:
            print("티스토리 홈페이지 접속 중...")
            self.driver.get(url)
            time.sleep(3)

            print("\"카카오계정으로 시작하기\" 버튼 찾는 중...")
            login_button = self.wait.until(
                EC.element_to_be_clickable((By.XPATH, "//*[contains(text(), '카카오계정으로 시작하기')]"))
            )
            login_button.click()
            time.sleep(2)

            print("\"카카오계정으로 로그인\" 버튼 클릭 중...")
            kakao_login_button = self.wait.until(
                EC.element_to_be_clickable((By.XPATH, "//*[contains(text(), '카카오계정으로 로그인')]"))
            )
            kakao_login_button.click()
            time.sleep(2)

            print("아이디 입력 중...")
            id_input = self.wait.until(
                EC.presence_of_element_located((By.ID, "loginId--1"))
            )
            id_input.clear()
            id_input.send_keys(self.id)
            time.sleep(1)

            print("비밀번호 입력 중...")
            password_input = self.wait.until(
                EC.presence_of_element_located((By.ID, "password--2"))
            )
            password_input.clear()
            password_input.send_keys(self.password)
            time.sleep(1)

            print("아이디와 비밀번호 입력 완료")

            print("로그인 버튼 찾는 중...")
            login_button = self.wait.until(
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

    def write_content(self, title: str, content: str, tags: List[str] = None):
        print("글쓰기 페이지로 직접 이동 중...")
        current_url = self.driver.current_url
        if "tistory.com" in current_url:
            if current_url.startswith("https://www.tistory.com"):
                blog_url = "https://hoons2641.tistory.com/manage/newpost"
            else:
                blog_url = "https://hoons2641.tistory.com/manage/newpost"

        self.driver.get(blog_url)
        time.sleep(5)

        print("제목 입력 중...")
        try:
            title_input = self.wait.until(
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
            iframe = self.wait.until(
                EC.presence_of_element_located(
                    (By.XPATH, "//iframe[contains(@title, 'Rich Text Area') or contains(@id, 'editor')]"))
            )
            self.driver.switch_to.frame(iframe)

            body = self.wait.until(
                EC.presence_of_element_located((By.TAG_NAME, "body"))
            )
            body.clear()
            body.send_keys(content)

            self.driver.switch_to.default_content()
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
                        content_area = self.driver.find_element(By.XPATH, selector)
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
                tag_input = self.wait.until(
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
                complete_button = self.wait.until(
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
                public_option = self.wait.until(
                    EC.element_to_be_clickable((By.XPATH, "//*[text()='공개']"))
                )
                public_option.click()
                time.sleep(1)
                print("✅ 공개 설정 완료!")

                publish_button = self.wait.until(
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
                            publish_btn = self.driver.find_element(By.XPATH, selector)
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

    def post_content(self,
                     blog_type: str,
                     url: str,
                     title: str,
                     content: str,
                     tags: List[str] = None):
        """
        블로그 포스트 작성
        :param blog_type: 블로그 종류
        :param url: 블로그 포스트 URL
        :param title: 블로그 포스트 제목
        :param content: 블로그 포스트 내용
        :param tags: 블로그 포스트 태그 리스트
        """

        if not self.login_up_to_password_input(url):
            print("로그인 과정에서 오류가 발생하여 게시물 작성을 중단합니다.")
            return False

        self.write_content(title, content, tags)
        print("블로그 포스트 작성 완료")
        return True

    def close_driver(self):
        """드라이버 종료"""
        if self.driver:
            self.driver.quit()
            self.crawling_service.close()


if __name__ == "__main__":
    service = BlogPostService()
    try:
        success = service.post_content("tistory",
                                       "https://www.tistory.com",
                                       "안녕하세요",
                                       "Hello World",
                                       ["테스트", "자동화"])
        if success:
            print("블로그 포스트 작성이 완료되었습니다.")
        else:
            print("블로그 포스트 작성에 실패했습니다.")
    except KeyboardInterrupt:
        print("\n프로그램이 중단되었습니다.")
    finally:
        input("Enter 키를 누르면 브라우저를 닫습니다...")
        service.close_driver()
