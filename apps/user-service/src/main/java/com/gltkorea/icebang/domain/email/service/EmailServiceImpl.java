package com.gltkorea.icebang.domain.email.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import com.gltkorea.icebang.domain.email.dto.EmailRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(EmailService.class)
public class EmailServiceImpl implements EmailService {
  @Override
  public void send(EmailRequest emailRequest) {}
}
