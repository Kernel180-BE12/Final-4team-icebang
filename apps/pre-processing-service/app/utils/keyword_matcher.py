from app.core.config import settings  # pydantic_settings 기반

try:
    import MeCab

    print("MeCab 라이브러리 로딩 성공")
    MECAB_AVAILABLE = True
except ImportError:
    print("MeCab 라이브러리를 찾을 수 없습니다. pip install mecab-python3 를 실행해주세요.")
    MeCab = None
    MECAB_AVAILABLE = False


class KeywordMatcher:
    """키워드 매칭 분석기"""

    def __init__(self):
        self.konlpy_available = False

        # MeCab 사용 가능 여부 확인
        if MECAB_AVAILABLE:
            try:
                # 경로가 있으면 사용, 없으면 기본값
                if settings.mecab_path:
                    self.mecab = MeCab.Tagger(f"-d {settings.mecab_path}")
                else:
                    self.mecab = MeCab.Tagger()  # 기본 경로

                # 테스트 실행
                test_result = self.mecab.parse("테스트")
                if test_result and test_result.strip():
                    self.konlpy_available = True
                    print(f"MeCab 형태소 분석기 사용 가능 (경로: {settings.mecab_path or '기본'})")
                else:
                    print("MeCab 테스트 실패")
            except Exception as e:
                print(f"MeCab 사용 불가 (규칙 기반으로 대체): {e}")
        else:
            print("MeCab 라이브러리가 설치되지 않았습니다. 규칙 기반으로 대체합니다.")

    def analyze_keyword_match(self, title: str, keyword: str) -> dict:
        """키워드 매칭 분석 결과 반환"""
        title_lower = title.lower().strip()
        keyword_lower = keyword.lower().strip()

        # 1. 완전 포함 검사
        exact_match = keyword_lower in title_lower
        if exact_match:
            return {
                'is_match': True,
                'match_type': 'exact',
                'score': 1.0,
                'reason': f"완전 포함: '{keyword}' in '{title[:50]}'"
            }

        # 2. 형태소 분석 (MeCab 사용)
        if self.konlpy_available:
            morphological_result = self._morphological_match(title_lower, keyword_lower)
            if morphological_result['is_match']:
                return morphological_result

        # 3. 규칙 기반 분석 (MeCab 실패시)
        simple_result = self._simple_keyword_match(title_lower, keyword_lower)
        return simple_result

    def _morphological_match(self, title: str, keyword: str) -> dict:
        """형태소 분석 기반 매칭"""
        try:
            # 키워드 형태소 분석
            keyword_result = self.mecab.parse(keyword)
            keyword_morphs = []
            for line in keyword_result.split('\n'):
                if line == 'EOS' or line == '':
                    continue
                parts = line.split('\t')
                if len(parts) >= 1:
                    morph = parts[0].strip()
                    if len(morph) >= 1:
                        keyword_morphs.append(morph)

            # 제목 형태소 분석
            title_result = self.mecab.parse(title)
            title_morphs = []
            for line in title_result.split('\n'):
                if line == 'EOS' or line == '':
                    continue
                parts = line.split('\t')
                if len(parts) >= 1:
                    morph = parts[0].strip()
                    if len(morph) >= 1:
                        title_morphs.append(morph)

            # 형태소 매칭
            matched = 0
            for kw in keyword_morphs:
                if len(kw) >= 2:  # 의미있는 형태소만 검사
                    for tw in title_morphs:
                        if kw == tw or kw in tw or tw in kw:
                            matched += 1
                            break

            match_ratio = matched / len(keyword_morphs) if keyword_morphs else 0
            threshold = 0.4

            if match_ratio >= threshold:
                return {
                    'is_match': True,
                    'match_type': 'morphological',
                    'score': match_ratio,
                    'reason': f"형태소 매칭: {matched}/{len(keyword_morphs)} = {match_ratio:.3f}"
                }

        except Exception as e:
            print(f"형태소 분석 오류: {e}")

        return {'is_match': False, 'match_type': 'morphological', 'score': 0.0, 'reason': '형태소 분석 실패'}

    def _simple_keyword_match(self, title: str, keyword: str) -> dict:
        """간단한 키워드 매칭"""
        # 공백으로 분리
        title_words = title.split()
        keyword_words = keyword.split()

        matched = 0
        for kw in keyword_words:
            if len(kw) >= 2:
                for tw in title_words:
                    if kw in tw or tw in kw:
                        matched += 1
                        break

        match_ratio = matched / len(keyword_words) if keyword_words else 0
        threshold = 0.3

        if match_ratio >= threshold:
            return {
                'is_match': True,
                'match_type': 'simple',
                'score': match_ratio,
                'reason': f"규칙 기반 매칭: {matched}/{len(keyword_words)} = {match_ratio:.3f}"
            }

        return {
            'is_match': False,
            'match_type': 'simple',
            'score': match_ratio,
            'reason': f"규칙 기반 미달: {matched}/{len(keyword_words)} = {match_ratio:.3f} < {threshold}"
        }