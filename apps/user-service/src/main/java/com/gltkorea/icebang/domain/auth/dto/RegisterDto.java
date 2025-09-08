package com.gltkorea.icebang.domain.auth.dto;

import java.math.BigInteger;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
  @Null private BigInteger id;

  @NotBlank(message = "사용자명은 필수입니다")
  private String name;

  @NotBlank(message = "이메일은 필수입니다")
  @Email(message = "올바른 이메일 형식이 아닙니다")
  private String email;

  @Null private BigInteger userOrgId;

  @NotNull(message = "조직 선택은 필수입니다")
  private BigInteger orgId;

  @NotNull(message = "부서 선택은 필수입니다")
  private BigInteger deptId;

  @NotNull(message = "직책 선택은 필수입니다")
  private BigInteger positionId;

  @NotNull(message = "역할 선택은 필수입니다")
  private Set<BigInteger> roleIds;

  @Null private String password;

  @Null private String status;
}
