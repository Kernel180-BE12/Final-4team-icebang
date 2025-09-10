package site.icebang.domain.email.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.email.dto.EmailRequest;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(EmailService.class)
public class EmailServiceImpl implements EmailService {
  @Override
  public void send(EmailRequest emailRequest) {}
}
