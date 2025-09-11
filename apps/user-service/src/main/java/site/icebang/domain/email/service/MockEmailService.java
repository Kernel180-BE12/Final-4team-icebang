package site.icebang.domain.email.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import site.icebang.domain.email.dto.EmailRequest;

@Service
// @Profile({"test-unit", "test-e2e", "test-integration", "local", "develop", "production"})
@Slf4j
public class MockEmailService implements EmailService {

  @Override
  public void send(EmailRequest emailRequest) {
    log.info("Mock send mail to: {}", emailRequest.getTo());
    log.info("Subject: {}", emailRequest.getSubject());
    log.info("Body: {}", emailRequest.getBody());
  }
}
