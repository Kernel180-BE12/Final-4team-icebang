package com.gltkorea.icebang.common.utils;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

@Component
public class RandomPasswordGenerator {

  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String DIGITS = "0123456789";
  private static final String SPECIAL_CHARS = "!@#$%^&*()-_+=<>?";

  private final SecureRandom random = new SecureRandom();

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

  // 특정 문자열에서 랜덤으로 한 문자 선택
  private char getRandomChar(String charSet) {
    int randomIndex = random.nextInt(charSet.length());
    return charSet.charAt(randomIndex);
  }

  // 기본 길이로 비밀번호 생성
  public String generate() {
    return generate(12); // 기본 길이를 12로 설정
  }
}
