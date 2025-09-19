package site.icebang.domain.workflow.runner;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import site.icebang.domain.execution.model.TaskRun;
import site.icebang.domain.workflow.model.Task;

@Slf4j
@Component("httpTaskRunner") // "httpTaskRunner"라는 이름의 Bean으로 등록
@RequiredArgsConstructor
public class HttpTaskRunner implements TaskRunner {

  private final RestTemplate restTemplate;

  // private final TaskIoDataRepository taskIoDataRepository; // TODO: 입출력 저장을 위해 주입

  @Override
  public TaskExecutionResult execute(Task task, TaskRun taskRun, ObjectNode requestBody) {
    JsonNode params = task.getParameters();
    if (params == null) {
      return TaskExecutionResult.failure("Task에 파라미터가 정의되지 않았습니다.");
    }

    String url = params.path("url").asText();
    String method = params.path("method").asText("POST"); // 기본값 POST

    if (url.isEmpty()) {
      return TaskExecutionResult.failure("Task 파라미터에 'url'이 없습니다.");
    }

    try {
      // 1. HTTP 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      // 2. HTTP 요청 엔티티 생성 (헤더 + 동적 Body)
      HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

      log.debug("HTTP Task 요청: URL={}, Method={}, Body={}", url, method, requestBody.toString());

      // 3. RestTemplate으로 API 호출
      ResponseEntity<String> responseEntity =
          restTemplate.exchange(
              url, HttpMethod.valueOf(method.toUpperCase()), requestEntity, String.class);

      String responseBody = responseEntity.getBody();
      log.debug("HTTP Task 응답: Status={}, Body={}", responseEntity.getStatusCode(), responseBody);

      // TODO: taskIoDataRepository를 사용하여 requestBody와 responseBody를 DB에 저장

      return TaskExecutionResult.success(responseBody);

    } catch (RestClientException e) {
      log.error("HTTP Task 실행 중 에러 발생: TaskRunId={}, Error={}", taskRun.getId(), e.getMessage());
      return TaskExecutionResult.failure(e.getMessage());
    }
  }
}
