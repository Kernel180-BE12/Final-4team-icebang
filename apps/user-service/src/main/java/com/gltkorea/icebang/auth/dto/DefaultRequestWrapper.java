package com.gltkorea.icebang.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class DefaultRequestWrapper implements AuthRequestWrapper {
  private final LoginDto loginDto;
  private final SignUpDto signUpDto;
}
