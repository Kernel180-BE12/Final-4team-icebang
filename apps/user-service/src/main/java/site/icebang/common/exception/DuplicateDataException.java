package site.icebang.common.exception;

/**
 * 데이터 중복 상황에서 발생하는 예외 클래스입니다.
 *
 * <p>이 예외는 데이터베이스나 컬렉션에서 이미 존재하는 데이터를 중복해서 생성하거나 저장하려고 할 때 발생합니다. 주로 유니크 제약 조건 위반이나 비즈니스 로직상 중복을
 * 허용하지 않는 경우에 사용됩니다.
 *
 * <h2>사용 예제:</h2>
 *
 * <pre>{@code
 * // 사용자 이메일 중복 체크
 * if (userRepository.existsByEmail(email)) {
 *     throw new DuplicateDataException("이미 존재하는 이메일입니다: " + email);
 * }
 *
 * // 상품 코드 중복 체크
 * try {
 *     productService.createProduct(product);
 * } catch (DataIntegrityViolationException e) {
 *     throw new DuplicateDataException("중복된 상품 코드입니다", e);
 * }
 * }</pre>
 *
 * @author jys01012@gmail.com
 * @since v0.0.1-alpha
 */
public class DuplicateDataException extends RuntimeException {

  /**
   * 상세 메시지 없이 새로운 {@code DuplicateDataException}을 생성합니다.
   *
   * @since v0.0.1-alpha
   */
  public DuplicateDataException() {
    super();
  }

  /**
   * 지정된 상세 메시지와 함께 새로운 {@code DuplicateDataException}을 생성합니다.
   *
   * @param message 상세 메시지 (나중에 {@link Throwable#getMessage()} 메서드로 검색됨)
   * @since v0.0.1-alpha
   */
  public DuplicateDataException(String message) {
    super(message);
  }

  /**
   * 지정된 상세 메시지와 원인과 함께 새로운 {@code DuplicateDataException}을 생성합니다.
   *
   * <p>{@code cause}와 연관된 상세 메시지가 이 예외의 상세 메시지에 자동으로 포함되지는 않습니다.
   *
   * @param message 상세 메시지 (나중에 {@link Throwable#getMessage()} 메서드로 검색됨)
   * @param cause 원인 (나중에 {@link Throwable#getCause()} 메서드로 검색됨). {@code null} 값이 허용되며, 원인이 존재하지 않거나
   *     알 수 없음을 나타냄
   * @since v0.0.1-alpha
   */
  public DuplicateDataException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * 지정된 원인과 상세 메시지와 함께 새로운 {@code DuplicateDataException}을 생성합니다. 상세 메시지는 {@code (cause==null ?
   * null : cause.toString())}로 설정됩니다. (일반적으로 {@code cause}의 클래스와 상세 메시지를 포함)
   *
   * @param cause 원인 (나중에 {@link Throwable#getCause()} 메서드로 검색됨). {@code null} 값이 허용되며, 원인이 존재하지 않거나
   *     알 수 없음을 나타냄
   * @since v0.0.1-alpha
   */
  public DuplicateDataException(Throwable cause) {
    super(cause);
  }

  /**
   * 지정된 상세 메시지, 원인, suppression 활성화 여부, 그리고 writable stack trace 여부와 함께 새로운 {@code
   * DuplicateDataException}을 생성합니다.
   *
   * @param message 상세 메시지
   * @param cause 원인. ({@code null} 값이 허용되며, 원인이 존재하지 않거나 알 수 없음을 나타냄)
   * @param enableSuppression suppression이 활성화되는지 또는 비활성화되는지 여부
   * @param writableStackTrace stack trace가 writable해야 하는지 여부
   * @since v0.0.1-alpha
   */
  protected DuplicateDataException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
