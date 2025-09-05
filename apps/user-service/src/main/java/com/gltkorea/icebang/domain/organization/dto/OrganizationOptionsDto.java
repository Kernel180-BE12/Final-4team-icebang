package com.gltkorea.icebang.domain.organization.dto;

import java.util.List;

import com.gltkorea.icebang.domain.department.dto.DepartmentsCardDto;
import com.gltkorea.icebang.domain.position.dto.PositionCardDto;
import com.gltkorea.icebang.domain.roles.dto.RolesCardDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class OrganizationOptionsDto {
  List<DepartmentsCardDto> departments;
  List<PositionCardDto> positions;
  List<RolesCardDto> roles;
}
