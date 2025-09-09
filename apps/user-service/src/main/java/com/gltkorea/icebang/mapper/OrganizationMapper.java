package com.gltkorea.icebang.mapper;

import java.math.BigInteger;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.gltkorea.icebang.domain.department.dto.DepartmentCardDo;
import com.gltkorea.icebang.domain.organization.dto.OrganizationCardDto;
import com.gltkorea.icebang.domain.position.dto.PositionCardDto;
import com.gltkorea.icebang.domain.roles.dto.RoleCardDto;

@Mapper
public interface OrganizationMapper {
  List<OrganizationCardDto> findAllOrganizations();

  List<DepartmentCardDo> findDepartmentsByOrganizationId(
      @Param("organizationId") BigInteger organizationId);

  List<PositionCardDto> findPositionsByOrganizationId(
      @Param("organizationId") BigInteger organizationId);

  List<RoleCardDto> findRolesByOrganizationId(@Param("organizationId") BigInteger organizationId);
}
