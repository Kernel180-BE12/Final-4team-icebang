package site.icebang.external.fastapi.adapter;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.icebang.global.config.properties.FastApiProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiAdapter {

    private final RestTemplate restTemplate;
    private final FastApiProperties properties;

    /**
     * FastAPI 서버에 API 요청을 보내는 범용 메소드
     * @param endpoint /keywords/search 와 같은 endpoint 경로
     * @param method HTTP 메소드
     * @param requestBody 요청 Body (JSON 문자열)
     * @return 성공 시 응답 Body, 실패 시 null
     */
    public String call(String endpoint, HttpMethod method, String requestBody) {
        String fullUrl = properties.getUrl() + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("FastAPI 요청: URL={}, Method={}, Body={}", fullUrl, method, requestBody);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    fullUrl,
                    method,
                    requestEntity,
                    String.class
            );

            String responseBody = responseEntity.getBody();
            log.debug("FastAPI 응답: Status={}, Body={}", responseEntity.getStatusCode(), responseBody);
            return responseBody;

        } catch (RestClientException e) {
            log.error("FastAPI 호출 실패: URL={}, Error={}", fullUrl, e.getMessage());
            return null;
        }
    }
}