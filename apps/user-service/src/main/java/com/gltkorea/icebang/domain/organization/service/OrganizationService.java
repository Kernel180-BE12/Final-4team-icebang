package com.gltkorea.icebang.domain.organization.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gltkorea.icebang.domain.department.dto.DepartmentCardDo;
import com.gltkorea.icebang.domain.organization.dto.OrganizationCardDto;
import com.gltkorea.icebang.domain.organization.dto.OrganizationOptionDto;
import com.gltkorea.icebang.domain.organization.mapper.OrganizationMapper;
import com.gltkorea.icebang.domain.position.dto.PositionCardDto;
import com.gltkorea.icebang.domain.roles.dto.RoleCardDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationService {
  private final OrganizationMapper organizationMapper;

  @Transactional(readOnly = true)
  public List<OrganizationCardDto> getAllOrganizationList() {
    return organizationMapper.findAllOrganizations();
  }

  public OrganizationOptionDto getOrganizationOptions(BigInteger id) {
    List<DepartmentCardDo> departments = organizationMapper.findDepartmentsByOrganizationId(id);
    List<PositionCardDto> positions = organizationMapper.findPositionsByOrganizationId(id);
    List<RoleCardDto> roles = organizationMapper.findRolesByOrganizationId(id);

    return OrganizationOptionDto.builder()
        .departments(departments)
        .positions(positions)
        .roles(roles)
        .build();
  }
}
