from bs4 import BeautifulSoup, Comment
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
import re


def preprocess_html(html_content):
    """
    HTML 전처리
    :param html_content: 원본 HTML 문자열
    :return: 전처리된 HTML 문자열 리스트
    """
    soup = BeautifulSoup(html_content, "html.parser")

    # 불필요한 태그 제거
    unnecessary_tags = [
        "script",  # JavaScript 코드
        "style",  # CSS 스타일
        "noscript",  # JavaScript 비활성화 시 내용
        "meta",  # 메타데이터
        "link",  # 외부 리소스 링크 (중요한 것 제외)
        "head",  # head 전체
        "title",  # 페이지 제목
        "base",  # base URL
    ]

    for tag_name in unnecessary_tags:
        for tag in soup.find_all(tag_name):
            if tag_name == "link" and tag.get("rel") in ["stylesheet", "icon"]:
                continue
            tag.decompose()

    # HTML 주석 제거
    comments = soup.find_all(string=lambda text: isinstance(text, Comment))
    for comment in comments:
        comment.extract()

    # display:none만 제거하고 다른 숨김 요소는 보존
    hidden_elements = soup.find_all(
        attrs={"style": re.compile(r"display\s*:\s*none", re.I)}
    )
    for element in hidden_elements:
        element.decompose()

    # 중요한 속성들을 보존
    important_attributes = {
        "id",
        "class",
        "name",
        "type",
        "value",
        "href",
        "src",
        "alt",
        "title",
        "placeholder",
        "role",
        "aria-label",
        "aria-describedby",
        "aria-expanded",
        "onclick",
        "onchange",
        "onfocus",
        "onblur",
        "disabled",
        "readonly",
        "required",
        "checked",
        "selected",
        "hidden",
        "tabindex",
        "contenteditable",
        "spellcheck",
        "autocomplete",
        "maxlength",
        "minlength",
        "for",
        "form",
        "method",
        "action",
        "target",
    }

    for tag in soup.find_all(True):
        attrs_to_remove = []
        for attr_name in tag.attrs.keys():
            # data-* 속성은 모두 보존
            if attr_name.startswith("data-"):
                continue
            # aria-* 속성도 모두 보존
            if attr_name.startswith("aria-"):
                continue
            # on* 이벤트 속성들도 보존
            if attr_name.startswith("on"):
                continue
            # 중요 속성이 아니면 제거
            if attr_name not in important_attributes:
                attrs_to_remove.append(attr_name)

        for attr_name in attrs_to_remove:
            del tag.attrs[attr_name]

    # 빈 태그 제거
    interactive_tags = {
        "input",
        "button",
        "select",
        "textarea",
        "a",
        "img",
        "br",
        "hr",
        "div",
        "span",
    }

    def remove_empty_tags_conservative():
        removed_any = True
        iteration = 0
        while removed_any and iteration < 3:  # 최대 3번만 반복
            removed_any = False
            iteration += 1

            for tag in soup.find_all():
                # 상호작용 가능한 태그는 보존
                if tag.name in interactive_tags:
                    continue

                # contenteditable 속성이 있으면 보존
                if tag.get("contenteditable"):
                    continue

                # data-* 속성이 있으면 보존
                if any(attr.startswith("data-") for attr in tag.attrs.keys()):
                    continue

                # 텍스트도 없고 자식 요소도 없으면 제거
                if not tag.get_text(strip=True) and not tag.find_all():
                    tag.decompose()
                    removed_any = True

    remove_empty_tags_conservative()

    # 연속된 공백 정리
    for text_node in soup.find_all(string=True):
        if text_node.parent.name not in ["script", "style"]:
            cleaned_text = re.sub(r"\s+", " ", str(text_node))
            if cleaned_text != str(text_node):
                text_node.replace_with(cleaned_text)

    html_list = _chunking_html(str(soup))
    return html_list


def _chunking_html(html_content, chunk_size=50000):
    """
    HTML을 지정된 크기로 분할하는 메서드
    :param html_content: 원본 HTML 문자열
    :param chunk_size: 각 청크의 최대 크기 (문자 수)
    :return: HTML 청크 리스트
    """
    chunks = []
    for i in range(0, len(html_content), chunk_size):
        chunks.append(html_content[i : i + chunk_size])
    return chunks


def wait_for_tistory_editor_complete(driver, timeout=30):
    """
    티스토리 TinyMCE 에디터가 완전히 로드될 때까지 대기
    """
    from selenium.webdriver.support.ui import WebDriverWait

    wait = WebDriverWait(driver, timeout)

    # 페이지 기본 로딩
    wait.until(lambda d: d.execute_script("return document.readyState") == "complete")

    # TinyMCE 라이브러리 로딩
    wait.until(lambda d: d.execute_script("return typeof tinymce !== 'undefined'"))

    # 에디터 인스턴스 초기화
    wait.until(
        lambda d: d.execute_script(
            """
        return tinymce.get('editor-tistory') && 
               tinymce.get('editor-tistory').initialized
    """
        )
    )

    # iframe 준비
    wait.until(EC.presence_of_element_located((By.ID, "editor-tistory_ifr")))

    # iframe 내부 document 준비
    wait.until(
        lambda d: d.execute_script(
            """
        try {
            var editor = tinymce.get('editor-tistory');
            var doc = editor.getDoc();
            return doc && doc.readyState === 'complete';
        } catch (e) {
            return false;
        }
    """
        )
    )

    return True
