package com.gltkorea.icebang.domain.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
  private String userName; // email
  private String password;
}
