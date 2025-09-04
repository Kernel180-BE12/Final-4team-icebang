package com.gltkorea.icebang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.gltkorea.icebang.domain.organization.dto.OrganizationCardDto;

@Mapper
public interface OrganizationMapper {
  List<OrganizationCardDto> findAllOrganizations();
}
