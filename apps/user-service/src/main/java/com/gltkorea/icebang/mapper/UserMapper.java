package com.gltkorea.icebang.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.gltkorea.icebang.dto.UserDto;

@Mapper // Spring이 MyBatis Mapper로 인식하도록 설정
public interface UserMapper {
  // XML 파일의 id와 메서드 이름을 일치시켜야 합니다.
  Optional<UserDto> findByEmail(String email);
}
