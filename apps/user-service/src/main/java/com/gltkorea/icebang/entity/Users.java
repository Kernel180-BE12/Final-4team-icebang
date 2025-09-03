package com.gltkorea.icebang.entity;

import lombok.Data;

@Data
// @TODO:: 우리 User entity에 맞게 설계
// @TODO:: 관련 테이블들도 구성해야함
public class Users {
  private String email;
  private String password;
}
