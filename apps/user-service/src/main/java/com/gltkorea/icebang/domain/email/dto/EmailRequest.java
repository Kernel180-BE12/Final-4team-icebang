package com.gltkorea.icebang.domain.email.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EmailRequest {
  private String to;
  private String subject;
  private String body;
  private List<String> cc;
  private List<String> bcc;
  private boolean isHtml;
}
