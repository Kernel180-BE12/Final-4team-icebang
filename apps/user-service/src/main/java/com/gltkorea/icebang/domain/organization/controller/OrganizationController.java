package com.gltkorea.icebang.domain.organization.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gltkorea.icebang.domain.organization.dto.OrganizationCardDto;
import com.gltkorea.icebang.domain.organization.service.OrganizationService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/v0/organizations")
@RequiredArgsConstructor
@RestController
public class OrganizationController {
  private final OrganizationService organizationService;

  @GetMapping("")
  public List<OrganizationCardDto> getOrganizations() {

    return organizationService.getAllOrganizationList();
  }
}
