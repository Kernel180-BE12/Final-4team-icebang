package site.icebang.common.utils;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

/**
 * 보안이 강화된 랜덤 패스워드를 생성하는 유틸리티 클래스입니다.
 *
 * <p>이 클래스는 소문자, 대문자, 숫자, 특수문자를 포함한 강력한 패스워드를 생성합니다. 생성된 패스워드는 각 문자 유형을 최소 하나씩 포함하도록 보장됩니다.
 *
 * <h3>사용 예제:</h3>
 *
 * <pre>{@code
 * @Autowired
 * private RandomPasswordGenerator passwordGenerator;
 *
 * // 기본 길이(12자)로 패스워드 생성
 * String password1 = passwordGenerator.generate();
 *
 * // 사용자 지정 길이로 패스워드 생성
 * String password2 = passwordGenerator.generate(16);
 * }</pre>
 *
 * @author jys01012@gmail.com
 * @since v0.0.1-alpha
 */
@Component
public class RandomPasswordGenerator {

  /** 소문자 알파벳 문자 집합 */
  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";

  /** 대문자 알파벳 문자 집합 */
  private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /** 숫자 문자 집합 */
  private static final String DIGITS = "0123456789";

  /** 특수문자 집합 */
  private static final String SPECIAL_CHARS = "!@#$%^&*()-_+=<>?";

  /** 암호학적으로 안전한 난수 생성기 */
  private final SecureRandom random = new SecureRandom();

  /**
   * 지정된 길이의 랜덤 패스워드를 생성합니다.
   *
   * <p>생성되는 패스워드는 다음 규칙을 준수합니다:
   *
   * <ul>
   *   <li>최소 길이는 8자입니다
   *   <li>소문자, 대문자, 숫자, 특수문자를 각각 최소 1개씩 포함합니다
   *   <li>모든 문자는 무작위로 섞여서 배치됩니다
   * </ul>
   *
   * @param length 생성할 패스워드의 길이 (8보다 작으면 8로 조정됩니다)
   * @return 생성된 랜덤 패스워드
   * @throws IllegalArgumentException length가 음수인 경우
   * @since v0.0.1-alpha
   */
  public String generate(int length) {
    if (length < 8) {
      length = 8;
    }

    StringBuilder passwordBuilder = new StringBuilder();
    passwordBuilder.append(getRandomChar(LOWERCASE));
    passwordBuilder.append(getRandomChar(UPPERCASE));
    passwordBuilder.append(getRandomChar(DIGITS));
    passwordBuilder.append(getRandomChar(SPECIAL_CHARS));

    // 나머지 길이를 채우기 위해 모든 문자 집합을 사용
    String allChars = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARS;
    IntStream.range(4, length)
        .forEach(
            i -> {
              passwordBuilder.append(getRandomChar(allChars));
            });

    // 생성된 문자열을 리스트로 변환하여 섞기
    List<Character> passwordChars =
        passwordBuilder.chars().mapToObj(c -> (char) c).collect(Collectors.toList());

    Collections.shuffle(passwordChars, random);

    // 다시 문자열로 합치기
    return passwordChars.stream()
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
        .toString();
  }

  /**
   * 지정된 문자 집합에서 랜덤으로 한 문자를 선택합니다.
   *
   * @param charSet 선택할 문자들이 포함된 문자열
   * @return 선택된 랜덤 문자
   * @throws IllegalArgumentException charSet이 null이거나 빈 문자열인 경우
   * @since v0.0.1-alpha
   */
  private char getRandomChar(String charSet) {
    int randomIndex = random.nextInt(charSet.length());
    return charSet.charAt(randomIndex);
  }

  /**
   * 기본 길이(12자)로 랜덤 패스워드를 생성합니다.
   *
   * <p>이는 {@code generate(12)}를 호출하는 것과 동일합니다.
   *
   * @return 12자 길이의 랜덤 패스워드
   * @see #generate(int)
   * @since v0.0.1-alpha
   */
  public String generate() {
    return generate(12); // 기본 길이를 12로 설정
  }
}
