package com.gltkorea.icebang.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.gltkorea.icebang.domain.auth.dto.RegisterDto;

@Mapper
public interface AuthMapper {
  void registerUser(RegisterDto registerDto);
}
