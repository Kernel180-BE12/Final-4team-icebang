import os
from openai import OpenAI
from dotenv import load_dotenv

load_dotenv()


class LLMExtractor:

    def __init__(self, model="gpt-4o"):
        """
        LLMExtractor 초기화
        :param model: 사용할 LLM 모델 이름
        """

        self.client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))
        self.model = model

    def login_extraction_prompt(self, target_description: str, html: str):
        """
        네이버, 티스토리 통합 로그인 프롬프트
        :param html: 분석할 HTML
        :param target_description: 추출 대상 설명
        :return: 프롬프트 문자열
        """

        return f"""
        # 지시 (Instructions):
            1. 당신은 HTML에서 웹 자동화에 필요한 정확한 요소를 찾는 전문가입니다.
            2. 당신의 임무는 사용자의 목표와 가장 일치하는 요소에 대한 CSS Selector와 XPath를 정확하게 찾아내어 지정된 JSON 형식으로 반환하는 것입니다.
        
        # 규칙 (Rules):
            1. 만약 요청한 요소가 HTML 문서에 존재하지 않는다면, 반드시 {{"found": false}} 만 반환해야 합니다. 
            2. 억지로 추측하거나 존재하지 않는 요소에 대한 정보를 생성하지 마세요. 
            3. name에는 요소의 이름을 나타내도록 지정하세요. 예: id, password, login_button, title, body 등
            4. 반한되는 형식 :
             {{
                "found": true/false,
                "elements": [
                    {{
                        "name_css_selector": "CSS 선택자 문자열",
                        "name_xpath": "XPath 문자열"
                    }},
                ]
            }}
    
        # 수행 (Execution):    
            사용자의 요구 사항 : {target_description}
            HTML 문서 : {html}

        """

    def naver_post_extraction_prompt(self, html: str):
        """
        네이버 블로그 포스트 프롬프트
        :param html: 분석할 HTML
        :return: 프롬프트 문자열
        """

        return f"""
            # 지시 (Instructions):
                1. 당신은 HTML에서 웹 자동화에 필요한 정확한 요소를 찾는 전문가입니다.
                2. 당신의 임무는 목표(Goal)와 가장 일치하는 요소에 대한 CSS Selector와 XPath를 정확하게 찾아내어 지정된 JSON 형식으로 반환하는 것입니다.
            
            #  규칙 (Rules):
                1. 만약 요청한 요소가 HTML 문서에 존재하지 않는다면, 반드시 {{"found": false}} 만 반환해야 합니다. 
                2. 억지로 추측하거나 존재하지 않는 요소에 대한 정보를 생성하지 마세요. 
                
            # 목표 (Goal):
                
                ## 제목 입력 영역 찾기:
                "제목"이 포함된 요소 찾기
                   - HTML에서 "제목"이라는 한글 텍스트를 포함한 모든 요소 검색
                   - 이 요소와 같은 부모나 형제 관계에 있는 요소 찾기
                
                ## 본문 입력 영역 찾기:
                "본문"이 포함된 요소 찾기:
                    - HTML에서 "본문"이라는 한글 텍스트를 포함한 모든 요소 검색
                    - 이 요소와 같은 부모나 형제 관계에 있는 요소 찾기
                    
                # 도움말 닫기 버튼 찾기:
                "도움말"이 포함된 요소 찾기:
                    - "도움말"이라는 한글 텍스트를 포함한 모든 요소 검색
                    - 이 요소와 같은 부모나 형제 관계에 있는 "닫기" 버튼 찾기
                    
                # 첫 번째 발행 버튼(팝업 열기용) 찾기:
                "발행"이 포함된 버튼 요소 찾기:
                    - HTML에서 "발행"이라는 한글 텍스트를 포함한 모든 버튼
                    - 이 버튼이 팝업을 여는 역할을 하는지 확인
                
                # 태그 입력 필드 찾기:
                "tag"가 포함된 요소 찾기:
                    - HTML에서 "tag"라는 단어가 포함된 모든 요소 검색
                    - id나 placeholder에 "tag" or "태그" 관련 내용이 있는 것
                    
                # 최종 발행 버튼 찾기:
                popup 내부의 발행 버튼 찾기:
                    - popup div 내부에 있는 "발행" 버튼 
                    - confirm_btn 클래스가 포함된 버튼
        
            # 반환 형식:
            {{
                "found": true/false,
                "elements": [
                    {{
                        "title_css_selector": "제목 입력을 위한 요소의 CSS 선택자",
                        "title_xpath": "제목 입력을 위한 요소의 XPath"
                    }},
                    {{
                        "content_css_selector": "본문 입력을 위한 요소의 CSS 선택자",
                        "content_xpath": "본문 입력을 위한 요소의 XPath"
                    }},
                    {{
                        "help_close_css_selector": "도움말 닫기 버튼의 CSS 선택자",
                        "help_close_xpath": "도움말 닫기 버튼의 XPath"
                    }},
                    {{
                        "first_publish_css_selector": "첫 번째 발행 버튼(팝업 열기용)의 CSS 선택자",
                        "popup_publish_xpath": "첫 번째 발행 버튼(팝업 열기용)의 XPath"
                    }},
                    {{
                        "tag_input_css_selector": "태그 입력 필드의 CSS 선택자",
                        "tag_input_xpath": "태그 입력 필드의 XPath"
                    }},
                    {{
                        "final_publish_css_selector": "팝업 내의 발행 버튼의 CSS 선택자",
                        "final_publish_xpath": "팝업 내의 발행 버튼의 XPath"
                    }}
                ]
            }}
        
            # 분석할 HTML:
            {html}
            """

    def tistory_post_extraction_prompt(self, html: str):
        """
        티스토리 기본 입력 요소들 (제목, 내용, 태그, 완료버튼) 추출 프롬프트
        :param html: 분석할 HTML
        :return: 프롬프트 문자열
        """
        return f"""
            # 지시 (Instructions):
                1. 당신은 HTML에서 웹 자동화에 필요한 정확한 요소를 찾는 전문가입니다.
                2. 당신의 임무는 목표(Goal)와 가장 일치하는 요소에 대한 CSS Selector와 XPath를 정확하게 찾아내어 지정된 JSON 형식으로 반환하는 것입니다.

            #  규칙 (Rules):
                1. 만약 요청한 요소가 HTML 문서에 존재하지 않는다면, 반드시 {{"found": false}} 만 반환해야 합니다. 
                2. 억지로 추측하거나 존재하지 않는 요소에 대한 정보를 생성하지 마세요. 

            # 목표 (Goal):

            ## 제목 입력 영역 찾기:
            "제목"이 포함된 요소 찾기
               - HTML에서 "제목"이라는 한글 텍스트를 포함한 모든 요소 검색
               - 이 요소와 같은 부모나 형제 관계에 있는 요소 찾기

            ## 글 내용 입력 영역 찾기:
            "글 내용 입력"이 포함된 요소 찾기:
                - iframe 내부의 요소 우선 검색
                - "글 내용 입력"이라는 한글 텍스트를 포함한 요소 검색
                - contenteditable="true" 속성을 가진 요소 우선 검색

            # "tag" or "태그" 입력 필드 찾기:
            "tag" or "태그"가 포함된 요소 찾기:
                - HTML에서 "tag" or "태그"라는 텍스트를 포함한 모든 요소 검색
                - id나 placeholder에 "tag" or "태그" 관련 내용이 있는 것

            # 완료 버튼 찾기:
            "완료"가 포함된 버튼 요소 찾기:
                - HTML에서 정확히 "완료"라는 한글 텍스트를 포함한 모든 버튼
                - 이 버튼이 글 작성을 완료하는 역할을 하는지 확인

            # 반환 형식:
                    {{
                        "found": true/false,
                        "elements": [
                            {{
                                "title_css_selector": "제목 입력을 위한 요소의 CSS 선택자 또는 null",
                                "title_xpath": "제목 입력을 위한 요소의 XPath 또는 null"
                            }},
                            {{
                                "content_css_selector": "글 내용 입력을 위한 요소의 CSS 선택자 또는 null",
                                "content_xpath": "글 내용 입력을 위한 요소의 XPath 또는 null"
                            }},
                            {{
                                "tag_input_css_selector": "태그 입력 필드의 CSS 선택자 또는 null",
                                "tag_input_xpath": "태그 입력 필드의 XPath 또는 null"
                            }},
                            {{
                                "complete_css_selector": "완료 버튼의 CSS 선택자 또는 null",
                                "complete_xpath": "완료 버튼의 XPath 또는 null"
                            }}
                        ]
                    }}

            # 분석할 HTML:
            {html}
            """

    def tistory_publish_extraction_prompt(self, html: str):
        """
        티스토리 발행 관련 요소들 (공개 라디오, 발행 버튼) 추출 프롬프트
        완료 버튼 클릭 후 동적으로 생성되는 요소들을 찾기 위한 프롬프트
        :param html: 분석할 HTML (완료 버튼 클릭 후 업데이트된 HTML)
        :return: 프롬프트 문자열
        """
        return f"""
            # 지시 (Instructions):
                1. 당신은 HTML에서 웹 자동화에 필요한 정확한 요소를 찾는 전문가입니다.
                2. 당신의 임무는 목표(Goal)와 가장 일치하는 요소에 대한 CSS Selector와 XPath를 정확하게 찾아내어 지정된 JSON 형식으로 반환하는 것입니다.

            #  규칙 (Rules):
                1. 만약 요청한 요소가 HTML 문서에 존재하지 않는다면, 반드시 {{"found": false}} 만 반환해야 합니다. 
                2. 억지로 추측하거나 존재하지 않는 요소에 대한 정보를 생성하지 마세요. 
                3. CSS 선택자에서 Selenium이 지원하지 않는 문법을 사용하지 마세요:
                   - :contains() 선택자 금지 (jQuery 전용)
                   - :visible, :hidden 같은 jQuery 전용 선택자 금지
                   - 표준 CSS 선택자만 사용 (id, class, attribute, tag 등)
                
            # 목표 (Goal):

            # 공개 radio 버튼 찾기:
            "공개"가 포함된 radio 요소 찾기:
                - input type="radio" 요소 우선 검색
                - HTML에서 "공개"라는 한글 텍스트를 포함한 모든 radio 버튼
                - 글의 공개/비공개 설정을 위한 라디오 버튼

            # 발행 버튼 찾기:
            "발행"이 포함된 버튼 요소 찾기:
                - HTML에서 "발행"이라는 한글 텍스트를 포함한 모든 버튼
                - "게시", "Publish" 등의 유사한 텍스트도 포함
                - publish-btn, btn-publish 등의 id나 class를 가진 버튼 우선 검색
                - 이 버튼이 최종적으로 글을 발행하는 역할을 하는지 확인

            # 반환 형식:
                    {{
                        "found": true/false,
                        "elements": [
                            {{
                                "public_radio_css_selector": "공개 radio의 CSS 선택자 또는 null",
                                "public_radio_xpath": "공개 radio의 XPath 또는 null"
                            }},
                            {{
                                "publish_css_selector": "발행 버튼의 CSS 선택자 또는 null",
                                "publish_xpath": "발행 버튼의 XPath 또는 null"
                            }}
                        ]
                    }}

            # 분석할 HTML:
            {html}
            """
