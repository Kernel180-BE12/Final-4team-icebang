package site.icebang.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import site.icebang.domain.user.dto.CheckEmailRequest;
import site.icebang.domain.user.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  public Boolean isExistEmail(@Valid CheckEmailRequest request) {
    return userMapper.existsByEmail(request.getEmail());
  }
}
