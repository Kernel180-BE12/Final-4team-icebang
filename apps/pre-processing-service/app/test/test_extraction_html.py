# if __name__ == "__main__":
#     from app.utils.crawling_util import CrawlingUtil
#     from app.utils.llm_extractor import LLMExtractor
#     from selenium.webdriver.common.by import By
#     from selenium.webdriver.support import expected_conditions as EC
#     from selenium.common.exceptions import TimeoutException
#     from selenium.webdriver.common.keys import Keys
#     from selenium.webdriver.common.action_chains import ActionChains
#     import pyperclip
#     import time
#     import json
#
#     crawling_util = CrawlingUtil()
#     llm_extractor = LLMExtractor()
#
#     start_time = time.time()
#     driver = crawling_util.get_driver()
#     wait_driver = crawling_util.get_wait()
#
#     # ========== 로그인 부분 ==========
#     driver.get("https://nid.naver.com/nidlogin.login")
#     time.sleep(5)
#     html = driver.page_source
#
#     print(f"원본 HTML 길이: {len(html)}")
#     html_list = preprocess_html(html)
#
#     result_html = 0
#
#     for html in html_list:
#         result_html += len(html)
#
#     print(f"전처리된 HTML 총 길이: {result_html}, 분할된 청크 수: {len(html_list)}")
#
#     result = []
#
#     for idx, html in enumerate(html_list):
#         print(f"전처리된 HTML 길이: {len(html)}, List {idx}번 ")
#         prompt = llm_extractor.extraction_prompt("아이디, 비밀번호를 입력할 수 있는 요소, 로그인 버튼을 클릭할 수 있는 요소", html)
#
#         response = llm_extractor.client.chat.completions.create(
#             model=llm_extractor.model,
#             messages=[{"role": "system", "content": prompt}],
#             temperature=0,
#             response_format={"type": "json_object"}
#         )
#
#         result_json = response.choices[0].message.content
#
#         result.append(result_json)
#
#     parse_result = [json.loads(item) for item in result]
#     print(json.dumps(parse_result, indent=4, ensure_ascii=False))
#
#     # 로그인
#     naver_id = "all2641"
#     naver_password = "kdyn2641*"
#
#     # 모든 결과에서 요소들을 수집 (개선된 방식)
#     all_elements = {}
#
#     for item in parse_result:
#         if not item.get("found"):
#             print("요소를 찾지 못했습니다.")
#             continue
#
#         elements = item.get("elements", [])
#         for element in elements:
#             for key, value in element.items():
#                 # ID 관련 요소
#                 if "id" in key.lower():
#                     if "css_selector" in key:
#                         all_elements["id_css"] = value
#                     elif "xpath" in key:
#                         all_elements["id_xpath"] = value
#
#                 # Password 관련 요소
#                 elif "password" in key.lower() or "pw" in key.lower():
#                     if "css_selector" in key:
#                         all_elements["pw_css"] = value
#                     elif "xpath" in key:
#                         all_elements["pw_xpath"] = value
#
#                 # Login 관련 요소
#                 elif "login" in key.lower():
#                     if "css_selector" in key:
#                         all_elements["login_css"] = value
#                     elif "xpath" in key:
#                         all_elements["login_xpath"] = value
#
#     print(f"수집된 요소들: {all_elements}")
#
#     # 아이디 입력
#     id_input = None
#     if all_elements.get("id_css"):
#         try:
#             id_input = wait_driver.until(
#                 EC.presence_of_element_located((By.CSS_SELECTOR, all_elements["id_css"]))
#             )
#             print(f"아이디 요소 발견 (CSS): {all_elements['id_css']}")
#             time.sleep(2)
#         except TimeoutException:
#             print(f"아이디 요소를 찾지 못했습니다 (CSS): {all_elements['id_css']}")
#
#     if not id_input and all_elements.get("id_xpath"):
#         try:
#             id_input = wait_driver.until(
#                 EC.presence_of_element_located((By.XPATH, all_elements["id_xpath"]))
#             )
#             print(f"아이디 요소 발견 (XPath): {all_elements['id_xpath']}")
#             time.sleep(2)
#         except TimeoutException:
#             print(f"아이디 요소를 찾지 못했습니다 (XPath): {all_elements['id_xpath']}")
#
#     if id_input:
#         id_input.click()
#         time.sleep(1)
#         pyperclip.copy(naver_id)
#         time.sleep(1)
#         id_input.send_keys(Keys.COMMAND, "v")
#         time.sleep(1)
#
#     # 비밀번호 입력
#     password_input = None
#     if all_elements.get("pw_css"):
#         try:
#             password_input = wait_driver.until(
#                 EC.presence_of_element_located((By.CSS_SELECTOR, all_elements["pw_css"]))
#             )
#             print(f"비밀번호 요소 발견 (CSS): {all_elements['pw_css']}")
#             time.sleep(2)
#         except TimeoutException:
#             print(f"비밀번호 요소를 찾지 못했습니다 (CSS): {all_elements['pw_css']}")
#
#     if not password_input and all_elements.get("pw_xpath"):
#         try:
#             password_input = wait_driver.until(
#                 EC.presence_of_element_located((By.XPATH, all_elements["pw_xpath"]))
#             )
#             print(f"비밀번호 요소 발견 (XPath): {all_elements['pw_xpath']}")
#             time.sleep(2)
#         except TimeoutException:
#             print(f"비밀번호 요소를 찾지 못했습니다 (XPath): {all_elements['pw_xpath']}")
#
#     if password_input:
#         password_input.click()
#         time.sleep(1)
#         pyperclip.copy(naver_password)
#         time.sleep(1)
#         password_input.send_keys(Keys.COMMAND, "v")
#         time.sleep(1)
#
#     # 로그인 버튼 클릭
#     login_button = None
#     if all_elements.get("login_css"):
#         try:
#             login_selector = all_elements["login_css"].replace('\\', '')
#             login_button = wait_driver.until(
#                 EC.element_to_be_clickable((By.CSS_SELECTOR, login_selector))
#             )
#             print(f"로그인 버튼 요소 발견 (CSS): {login_selector}")
#         except TimeoutException:
#             print(f"로그인 버튼 요소를 찾지 못했습니다 (CSS): {all_elements['login_css']}")
#
#     if not login_button and all_elements.get("login_xpath"):
#         try:
#             login_button = wait_driver.until(
#                 EC.element_to_be_clickable((By.XPATH, all_elements["login_xpath"]))
#             )
#             print(f"로그인 버튼 요소 발견 (XPath): {all_elements['login_xpath']}")
#         except TimeoutException:
#             print(f"로그인 버튼 요소를 찾지 못했습니다 (XPath): {all_elements['login_xpath']}")
#
#     if login_button:
#         login_button.click()
#         print("로그인 버튼 클릭 완료")
#
#     # 로그인 완료 대기
#     time.sleep(5)
#     print("로그인 완료, 블로그 포스팅 시작...")
#
#     # ========== 블로그 포스팅 부분 (도움말 닫기 버튼 추가) ==========
#     try:
#         # 네이버 블로그 글쓰기 페이지로 이동
#         post_content_url = f"https://blog.naver.com/PostWriteForm.naver?blogId={naver_id}&Redirect=Write&redirect=Write&widgetTypeCall=true&noTrackingCode=true&directAccess=false"
#         driver.get(post_content_url)
#         print("블로그 글쓰기 페이지로 이동 완료. 5초 대기...")
#         time.sleep(10)
#
#         blog_html = driver.page_source
#         print(f"HTML 길이: {len(blog_html)}")
#         blog_html_list = preprocess_html(blog_html)
#         blog_result_html = sum(len(html) for html in blog_html_list)
#         print(f"전처리된 HTML 총 길이: {blog_result_html}, 분할된 청크 수: {len(blog_html_list)}")
#
#         # 테스트용 제목, 내용, 태그
#         test_title = "LLM 기반 자동화 포스팅"
#         test_content = "이 포스트는 LLM이 iframe 내부의 HTML을 분석하여 자동으로 작성한 글입니다."
#         test_tags = ["LLM", "자동화", "네이버블로그"]
#
#         # 3. LLM을 사용해 iframe 내부의 블로그 요소들 추출
#         blog_result = []
#
#         for idx, html in enumerate(blog_html_list):
#             print(f"HTML 청크 {idx + 1}/{len(blog_html_list)} 분석 중...")
#             prompt = llm_extractor.naver_post_extraction_prompt(html)
#             response = llm_extractor.client.chat.completions.create(
#                 model=llm_extractor.model,
#                 messages=[{"role": "system", "content": prompt}],
#                 temperature=0,
#                 response_format={"type": "json_object"}
#             )
#             blog_result.append(response.choices[0].message.content)
#
#         blog_parse_result = [json.loads(item) for item in blog_result]
#         print("\n>> 블로그 요소 추출 결과:")
#         print(json.dumps(blog_parse_result, indent=4, ensure_ascii=False))
#
#         # 4. 추출된 요소 정보 취합
#         blog_elements = {}
#         for item in blog_parse_result:
#             if not item.get("found"): continue
#             for element in item.get("elements", []):
#                 for key, value in element.items():
#                     if "title" in key.lower():
#                         if "css_selector" in key:
#                             blog_elements["title_css"] = value
#                         elif "xpath" in key:
#                             blog_elements["title_xpath"] = value
#                     elif "content" in key.lower() or "body" in key.lower():
#                         if "css_selector" in key:
#                             blog_elements["content_css"] = value
#                         elif "xpath" in key:
#                             blog_elements["content_xpath"] = value
#                     elif "help_close" in key.lower():
#                         if "css_selector" in key:
#                             blog_elements["help_close_css"] = value
#                         elif "xpath" in key:
#                             blog_elements["help_close_xpath"] = value
#                     elif "first_publish" in key.lower():
#                         if "css_selector" in key:
#                             blog_elements["first_publish_css"] = value
#                         elif "xpath" in key:
#                             blog_elements["first_publish_xpath"] = value
#                     elif "tag_input" in key.lower():
#                         if "css_selector" in key:
#                             blog_elements["tag_input_css"] = value
#                         elif "xpath" in key:
#                             blog_elements["tag_input_xpath"] = value
#                     elif "final_publish" in key.lower():
#                         if "css_selector" in key:
#                             blog_elements["final_publish_css"] = value
#                         elif "xpath" in key:
#                             blog_elements["final_publish_xpath"] = value
#
#         print(f"\n>> 수집된 블로그 요소들: {blog_elements}")
#
#         # 5. 도움말 닫기 버튼 클릭 (발행 버튼이 가려지지 않도록)
#         help_close_button = None
#         help_close_css = blog_elements.get("help_close_css")
#         if help_close_css:
#             try:
#                 help_close_button = wait_driver.until(EC.element_to_be_clickable((By.CSS_SELECTOR, help_close_css)))
#                 print(f"✅ 도움말 닫기 버튼 발견 (CSS): {help_close_css}")
#             except TimeoutException:
#                 print(f"⚠️ 도움말 닫기 버튼을 찾지 못했습니다 (CSS): {help_close_css}")
#
#         if not help_close_button:
#             help_close_xpath = blog_elements.get("help_close_xpath")
#             if help_close_xpath:
#                 try:
#                     help_close_button = wait_driver.until(EC.element_to_be_clickable((By.XPATH, help_close_xpath)))
#                     print(f"✅ 도움말 닫기 버튼 발견 (XPath): {help_close_xpath}")
#                 except TimeoutException:
#                     print(f"⚠️ 도움말 닫기 버튼을 찾지 못했습니다 (XPath): {help_close_xpath}")
#
#         if help_close_button:
#             try:
#                 help_close_button.click()
#                 print("✅ 도움말 닫기 버튼 클릭 완료")
#                 time.sleep(1)  # 닫히는 시간 대기
#             except Exception as e:
#                 print(f"⚠️ 도움말 닫기 버튼 클릭 실패: {str(e)}")
#                 # JavaScript로 강제 클릭 시도
#                 try:
#                     driver.execute_script("arguments[0].click();", help_close_button)
#                     print("✅ 도움말 닫기 버튼 JavaScript 클릭 완료")
#                     time.sleep(1)
#                 except Exception as js_e:
#                     print(f"❌ 도움말 닫기 버튼 JavaScript 클릭도 실패: {str(js_e)}")
#         else:
#             print("⚠️ 도움말 닫기 버튼을 찾지 못했습니다. se-utils 요소 직접 제거를 시도합니다.")
#             # 직접 se-utils 요소 제거
#             try:
#                 driver.execute_script("""
#                     var element = document.querySelector('.se-utils');
#                     if (element) {
#                         element.style.display = 'none';
#                         console.log('se-utils 요소를 숨겼습니다.');
#                     }
#                 """)
#                 print("✅ se-utils 요소를 직접 숨김 처리했습니다.")
#             except Exception as e:
#                 print(f"⚠️ se-utils 요소 숨김 처리 실패: {str(e)}")
#
#         # 6. 제목 및 본문 입력 (CSS, XPath 순차 시도)
#         # 제목 입력
#         title_input = None
#         title_css = blog_elements.get("title_css")
#         if title_css:
#             try:
#                 title_input = wait_driver.until(EC.element_to_be_clickable((By.CSS_SELECTOR, title_css)))
#                 print(f"✅ 제목 요소 발견 (CSS): {title_css}")
#             except TimeoutException:
#                 print(f"⚠️ 제목 요소를 찾지 못했습니다 (CSS): {title_css}")
#
#         if not title_input:
#             title_xpath = blog_elements.get("title_xpath")
#             if title_xpath:
#                 try:
#                     title_input = wait_driver.until(EC.element_to_be_clickable((By.XPATH, title_xpath)))
#                     print(f"✅ 제목 요소 발견 (XPath): {title_xpath}")
#                 except TimeoutException:
#                     print(f"⚠️ 제목 요소를 찾지 못했습니다 (XPath): {title_xpath}")
#
#         if title_input:
#             ActionChains(driver).move_to_element(title_input).click().send_keys(test_title).perform()
#             print("✅ 제목 입력 완료")
#         else:
#             print("❌ 제목 입력 요소를 최종적으로 찾지 못했습니다.")
#
#         # 본문 입력
#         content_input = None
#         content_css = blog_elements.get("content_css")
#         if content_css:
#             try:
#                 content_input = wait_driver.until(EC.element_to_be_clickable((By.CSS_SELECTOR, content_css)))
#                 print(f"✅ 본문 요소 발견 (CSS): {content_css}")
#             except TimeoutException:
#                 print(f"⚠️ 본문 요소를 찾지 못했습니다 (CSS): {content_css}")
#
#         if not content_input:
#             content_xpath = blog_elements.get("content_xpath")
#             if content_xpath:
#                 try:
#                     content_input = wait_driver.until(EC.element_to_be_clickable((By.XPATH, content_xpath)))
#                     print(f"✅ 본문 요소 발견 (XPath): {content_xpath}")
#                 except TimeoutException:
#                     print(f"⚠️ 본문 요소를 찾지 못했습니다 (XPath): {content_xpath}")
#
#         if content_input:
#             ActionChains(driver).move_to_element(content_input).click().send_keys(test_content).perform()
#             print("✅ 본문 입력 완료")
#         else:
#             print("❌ 본문 입력 요소를 최종적으로 찾지 못했습니다.")
#
#         # 7. 발행 버튼 클릭 (LLM이 찾은 선택자 사용)
#         first_publish_button = None
#         first_publish_css = blog_elements.get("first_publish_css")
#         if first_publish_css:
#             try:
#                 first_publish_button = wait_driver.until(
#                     EC.element_to_be_clickable((By.CSS_SELECTOR, first_publish_css)))
#                 print(f"✅ 첫 번째 발행 버튼 발견 (CSS): {first_publish_css}")
#             except TimeoutException:
#                 print(f"⚠️ 첫 번째 발행 버튼을 찾지 못했습니다 (CSS): {first_publish_css}")
#
#         if not first_publish_button:
#             first_publish_xpath = blog_elements.get("first_publish_xpath")
#             if first_publish_xpath:
#                 try:
#                     first_publish_button = wait_driver.until(
#                         EC.element_to_be_clickable((By.XPATH, first_publish_xpath)))
#                     print(f"✅ 첫 번째 발행 버튼 발견 (XPath): {first_publish_xpath}")
#                 except TimeoutException:
#                     print(f"⚠️ 첫 번째 발행 버튼을 찾지 못했습니다 (XPath): {first_publish_xpath}")
#
#         if first_publish_button:
#             try:
#                 # 일반 클릭 시도
#                 first_publish_button.click()
#                 print("✅ 첫 번째 발행 버튼 클릭 완료. 팝업창을 기다립니다...")
#             except Exception as click_error:
#                 print(f"⚠️ 일반 클릭 실패, JavaScript 클릭 시도: {str(click_error)}")
#                 driver.execute_script("arguments[0].click();", first_publish_button)
#                 print("✅ 첫 번째 발행 버튼 JavaScript 클릭 완료. 팝업창을 기다립니다...")
#
#             time.sleep(3)
#         else:
#             print("❌ 첫 번째 발행 버튼을 최종적으로 찾지 못했습니다. 하드코딩 선택자를 시도합니다.")
#             # 폴백: 하드코딩 선택자 사용
#             try:
#                 publish_button = wait_driver.until(
#                     EC.element_to_be_clickable((By.XPATH, "//button[.//span[normalize-space()='발행']]")))
#
#                 try:
#                     publish_button.click()
#                     print("✅ 발행 버튼 하드코딩 클릭 완료. 팝업창을 기다립니다...")
#                 except Exception as click_error:
#                     driver.execute_script("arguments[0].click();", publish_button)
#                     print("✅ 발행 버튼 하드코딩 JavaScript 클릭 완료. 팝업창을 기다립니다...")
#
#                 time.sleep(3)
#             except TimeoutException:
#                 print("❌ 하드코딩 발행 버튼도 찾지 못했습니다.")
#
#         # 8. 태그 입력 및 최종 발행 (LLM이 찾은 선택자 사용)
#         try:
#             # 태그 입력 필드 찾기
#             tag_input = None
#             tag_input_css = blog_elements.get("tag_input_css")
#             if tag_input_css:
#                 try:
#                     tag_input = wait_driver.until(EC.element_to_be_clickable((By.CSS_SELECTOR, tag_input_css)))
#                     print(f"✅ 태그 입력 필드 발견 (CSS): {tag_input_css}")
#                 except TimeoutException:
#                     print(f"⚠️ 태그 입력 필드를 찾지 못했습니다 (CSS): {tag_input_css}")
#
#             if not tag_input:
#                 tag_input_xpath = blog_elements.get("tag_input_xpath")
#                 if tag_input_xpath:
#                     try:
#                         tag_input = wait_driver.until(EC.element_to_be_clickable((By.XPATH, tag_input_xpath)))
#                         print(f"✅ 태그 입력 필드 발견 (XPath): {tag_input_xpath}")
#                     except TimeoutException:
#                         print(f"⚠️ 태그 입력 필드를 찾지 못했습니다 (XPath): {tag_input_xpath}")
#
#             if not tag_input:
#                 # 폴백: 하드코딩 선택자 사용
#                 tag_input = wait_driver.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "input[placeholder*='태그']")))
#                 print("✅ 태그 입력 필드 하드코딩 선택자로 발견")
#
#             # 태그 입력
#             for tag in test_tags:
#                 tag_input.send_keys(tag)
#                 tag_input.send_keys(Keys.ENTER)
#                 time.sleep(0.5)
#             print("✅ 태그 입력 완료")
#
#             # 최종 발행 버튼 찾기
#             final_publish_button = None
#             final_publish_css = blog_elements.get("final_publish_css")
#             if final_publish_css:
#                 try:
#                     final_publish_button = wait_driver.until(
#                         EC.element_to_be_clickable((By.CSS_SELECTOR, final_publish_css)))
#                     print(f"✅ 최종 발행 버튼 발견 (CSS): {final_publish_css}")
#                 except TimeoutException:
#                     print(f"⚠️ 최종 발행 버튼을 찾지 못했습니다 (CSS): {final_publish_css}")
#
#             if not final_publish_button:
#                 final_publish_xpath = blog_elements.get("final_publish_xpath")
#                 if final_publish_xpath:
#                     try:
#                         final_publish_button = wait_driver.until(
#                             EC.element_to_be_clickable((By.XPATH, final_publish_xpath)))
#                         print(f"✅ 최종 발행 버튼 발견 (XPath): {final_publish_xpath}")
#                     except TimeoutException:
#                         print(f"⚠️ 최종 발행 버튼을 찾지 못했습니다 (XPath): {final_publish_xpath}")
#
#             if not final_publish_button:
#                 # 폴백: 하드코딩 선택자 사용
#                 final_publish_button = wait_driver.until(EC.element_to_be_clickable(
#                     (By.XPATH, "//div[contains(@class,'popup')]//button[.//span[normalize-space()='발행']]")))
#                 print("✅ 최종 발행 버튼 하드코딩 선택자로 발견")
#
#             # 최종 발행 버튼 클릭
#             final_publish_button.click()
#             print("✅ 최종 발행 버튼 클릭 완료!")
#
#             wait_driver.until(EC.url_contains("PostView.naver"), timeout=10)
#             print("\n🎉 블로그 포스팅 발행 최종 완료! 🎉")
#         except TimeoutException:
#             print("❌ 발행 팝업 처리 중 오류가 발생했습니다.")
#             raise
#
#     except Exception as e:
#         print(f"블로그 포스팅 중 오류 발생: {str(e)}")
#
#     # ... (이후 전체 소요 시간 측정 및 드라이버 종료 코드) ...
#
#     end_time = time.time()
#     print(f"전체 소요 시간: {end_time - start_time} seconds")
#
#     # 대기 후 드라이버 종료
#     time.sleep(5)
#     driver.quit()
