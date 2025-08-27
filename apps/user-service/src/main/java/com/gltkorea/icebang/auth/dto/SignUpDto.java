package com.gltkorea.icebang.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDto {
  private String email; // 회원가입용 이메일
  private String password; // 비밀번호
  private String nickname; // 닉네임
}
