package com.gltkorea.icebang.domain.organization.dto;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrganizationCardDto {
  private BigInteger id;
  private String organizationName;
}
