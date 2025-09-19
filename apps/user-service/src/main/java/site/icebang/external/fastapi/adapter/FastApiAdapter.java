package site.icebang.external.fastapi.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import site.icebang.domain.workflow.model.Task;
import site.icebang.global.config.properties.FastApiProperties;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiAdapter {

    private final RestTemplate restTemplate;
    private final FastApiProperties properties;

    // 📌 Task나 context에 대한 의존성이 완전히 사라짐
    public String call(String endpoint, HttpMethod method, String requestBody) {
        String fullUrl = properties.getUrl() + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("FastAPI 요청: URL={}, Method={}, Body={}", fullUrl, method, requestBody);
            ResponseEntity<String> responseEntity = restTemplate.exchange(fullUrl, method, requestEntity, String.class);
            String responseBody = responseEntity.getBody();
            log.debug("FastAPI 응답: Status={}, Body={}", responseEntity.getStatusCode(), responseBody);
            return responseBody;
        } catch (RestClientException e) {
            log.error("FastAPI 호출 실패: URL={}, Error={}", fullUrl, e.getMessage());
            return null;
        }
    }
}