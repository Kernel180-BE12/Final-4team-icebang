package com.gltkorea.icebang.domain.organization.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gltkorea.icebang.domain.organization.dto.OrganizationCardDto;
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
}
