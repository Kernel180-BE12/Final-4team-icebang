package com.gltkorea.icebang.common.health.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FastApiClient {

  // WebConfig에서 생성하고 타임아웃이 설정된 RestTemplate Bean을 주입받습니다.
  private final RestTemplate restTemplate;

  // FastAPI 서버의 ping 엔드포인트 URL을 상수로 하드코딩합니다.
  private static final String FASTAPI_PING_URL = "http://localhost:8000/ping";

  /**
   * FastAPI 서버의 /ping 엔드포인트를 호출하여 연결을 테스트합니다.
   *
   * @return 연결 성공 시 FastAPI로부터 받은 응답, 실패 시 에러 메시지
   */
  public String ping() {
    log.info("Attempting to connect to FastAPI server at: {}", FASTAPI_PING_URL);

    try {
      // FastAPI 서버에 GET 요청을 보내고, 응답을 String으로 받습니다.
      // WebConfig에 설정된 5초 타임아웃이 여기서 적용됩니다.
      String response = restTemplate.getForObject(FASTAPI_PING_URL, String.class);
      log.info("Successfully received response from FastAPI: {}", response);
      return response;
    } catch (RestClientException e) {
      // RestClientException은 연결 실패, 타임아웃 등 모든 통신 오류를 포함합니다.
      log.error(
          "Failed to connect to FastAPI server at {}. Error: {}", FASTAPI_PING_URL, e.getMessage());
      return "ERROR: Cannot connect to FastAPI";
    }
  }
}
