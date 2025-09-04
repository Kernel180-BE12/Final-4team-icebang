package com.gltkorea.icebang.mapper;

import java.math.BigInteger;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.gltkorea.icebang.domain.department.dto.DepartmentsCardDto;
import com.gltkorea.icebang.domain.organization.dto.OrganizationCardDto;
import com.gltkorea.icebang.domain.position.dto.PositionCardDto;
import com.gltkorea.icebang.domain.roles.dto.RolesCardDto;

@Mapper
public interface OrganizationMapper {
  List<OrganizationCardDto> findAllOrganizations();

  List<DepartmentsCardDto> findDepartmentsByOrganizationId(
      @Param("organizationId") BigInteger organizationId);

  List<PositionCardDto> findPositionsByOrganizationId(
      @Param("organizationId") BigInteger organizationId);

  List<RolesCardDto> findRolesByOrganizationId(@Param("organizationId") BigInteger organizationId);
}
