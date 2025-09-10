package site.icebang.domain.email.service;

import site.icebang.domain.email.dto.EmailRequest;

public interface EmailService {
  void send(EmailRequest emailRequest);
}
