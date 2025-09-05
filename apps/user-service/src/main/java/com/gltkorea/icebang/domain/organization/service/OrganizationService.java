package com.gltkorea.icebang.domain.organization.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gltkorea.icebang.domain.department.dto.DepartmentsCardDto;
import com.gltkorea.icebang.domain.organization.dto.OrganizationCardDto;
import com.gltkorea.icebang.domain.organization.dto.OrganizationOptionsDto;
import com.gltkorea.icebang.domain.position.dto.PositionCardDto;
import com.gltkorea.icebang.domain.roles.dto.RolesCardDto;
import com.gltkorea.icebang.mapper.OrganizationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationService {
  private final OrganizationMapper organizationMapper;

  @Transactional(readOnly = true)
  public List<OrganizationCardDto> getAllOrganizationList() {
    return organizationMapper.findAllOrganizations();
  }

  public OrganizationOptionsDto getOrganizationOptions(BigInteger id) {
    List<DepartmentsCardDto> departments = organizationMapper.findDepartmentsByOrganizationId(id);
    List<PositionCardDto> positions = organizationMapper.findPositionsByOrganizationId(id);
    List<RolesCardDto> roles = organizationMapper.findRolesByOrganizationId(id);

    return OrganizationOptionsDto.builder()
        .departments(departments)
        .positions(positions)
        .roles(roles)
        .build();
  }
}
