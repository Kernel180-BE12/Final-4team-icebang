package com.gltkorea.icebang.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.gltkorea.icebang.domain.auth.dto.RegisterDto;
import com.gltkorea.icebang.domain.auth.model.AuthCredential;

@Mapper
public interface AuthMapper {
  AuthCredential findUserByEmail(String email);

  boolean existsByEmail(String email);

  int insertUser(RegisterDto dto); // users insert

  int insertUserOrganization(RegisterDto dto); // user_organizations insert

  int insertUserRoles(RegisterDto dto); // user_roles insert (foreach)
}
