package com.gltkorea.icebang.dto;

import lombok.Data;

@Data
public class FastApiCrawlingResponse {
  // FastAPI가 반환하는 JSON의 필드명과 일치해야 함
  private int price;
  private String stockStatus;
  private String productName; // 기타 필요한 정보
}
