package com.gltkorea.icebang.domain.email.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import com.gltkorea.icebang.domain.email.dto.EmailRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
//@ConditionalOnMissingBean(EmailService.class)
@Profile("develop")
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Override
  public void send(EmailRequest emailRequest) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(emailRequest.getTo());
      message.setSubject(emailRequest.getSubject());
      message.setText(emailRequest.getBody());
      message.setFrom("dev.icebang4@gmail.com");

      mailSender.send(message);

    } catch (Exception e) {
      throw new RuntimeException("이메일 발송 실패", e);
    }
    /*
     @TODO
    * 1. icebang email 설정
    * 2. email request 발송

    고려사항: email 전송 관련 bean, dependency, 구글 계정 설정 등 필요
    */


  }
}
