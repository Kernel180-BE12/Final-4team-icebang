package site.icebang.domain.organization.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import site.icebang.domain.department.dto.DepartmentCardDo;
import site.icebang.domain.position.dto.PositionCardDto;
import site.icebang.domain.roles.dto.RoleCardDto;

@Builder
@Data
@AllArgsConstructor
public class OrganizationOptionDto {
  List<DepartmentCardDo> departments;
  List<PositionCardDto> positions;
  List<RoleCardDto> roles;
}
