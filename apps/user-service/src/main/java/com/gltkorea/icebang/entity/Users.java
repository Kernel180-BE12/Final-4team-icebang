package com.gltkorea.icebang.entity;

import java.math.BigInteger;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Users {
  private BigInteger id;
  private String name;
  private String email;
  private String password;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
