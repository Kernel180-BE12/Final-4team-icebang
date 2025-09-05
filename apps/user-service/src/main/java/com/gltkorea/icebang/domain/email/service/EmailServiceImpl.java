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
  public void send(EmailRequest emailRequest) {
    /*
     @TODO
    * 1. icebang email 설정
    * 2. email request 발송

    고려사항: email 전송 관련 bean, dependency, 구글 계정 설정 등 필요
    */
  }
}
