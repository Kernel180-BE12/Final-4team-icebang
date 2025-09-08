package com.gltkorea.icebang.domain.organization.dto;

import java.util.List;

import com.gltkorea.icebang.domain.department.dto.DepartmentCardDo;
import com.gltkorea.icebang.domain.position.dto.PositionCardDto;
import com.gltkorea.icebang.domain.roles.dto.RoleCardDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class OrganizationOptionDto {
  List<DepartmentCardDo> departments;
  List<PositionCardDto> positions;
  List<RoleCardDto> roles;
}
