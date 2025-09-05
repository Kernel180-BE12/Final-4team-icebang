package com.gltkorea.icebang.domain.department.dto;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DepartmentsCardDto {
  private BigInteger id;
  private String name;
}
