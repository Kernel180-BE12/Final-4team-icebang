package com.gltkorea.icebang.domain.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
  @Select("SELECT COUNT(1) > 0 FROM users WHERE email = #{email}")
  boolean existsByEmail(@Param("email") String email);
}
