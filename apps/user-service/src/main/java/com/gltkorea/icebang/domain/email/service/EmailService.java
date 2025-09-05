package com.gltkorea.icebang.domain.email.service;

import com.gltkorea.icebang.domain.email.dto.EmailRequest;

public interface EmailService {
  void send(EmailRequest emailRequest);
}
