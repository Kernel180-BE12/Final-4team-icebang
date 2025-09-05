package com.gltkorea.icebang.domain.email.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.gltkorea.icebang.domain.email.dto.EmailRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Profile({"unit-test", "local", "develop"})
@Slf4j
public class MockEmailService implements EmailService {

  @Override
  public void send(EmailRequest emailRequest) {
    log.info("Mock send mail to: {}", emailRequest.getTo());
    log.info("Subject: {}", emailRequest.getSubject());
    log.info("Body: {}", emailRequest.getBody());
  }
}
