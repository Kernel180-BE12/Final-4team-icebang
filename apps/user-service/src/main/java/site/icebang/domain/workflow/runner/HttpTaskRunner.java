package site.icebang.domain.workflow.runner;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import site.icebang.domain.execution.model.TaskRun;
import site.icebang.domain.workflow.model.Task;

@Slf4j
@Component("httpTaskRunner")
@RequiredArgsConstructor
public class HttpTaskRunner implements TaskRunner {
  private final RestTemplate restTemplate;

  @Override
  public TaskExecutionResult execute(Task task, TaskRun taskRun) {
    JsonNode params = task.getParameters();
    String url = params.get("url").asText();
    String method = params.get("method").asText();
    JsonNode body = params.get("body");

    try {
      HttpEntity<String> requestEntity =
          new HttpEntity<>(
              body.toString(),
              new HttpHeaders() {
                {
                  setContentType(MediaType.APPLICATION_JSON);
                }
              });

      ResponseEntity<String> response =
          restTemplate.exchange(
              url, HttpMethod.valueOf(method.toUpperCase()), requestEntity, String.class);

      return TaskExecutionResult.success(response.getBody());
    } catch (RestClientException e) {
      log.error("HTTP Task 실행 실패: TaskRunId={}, Error={}", taskRun.getId(), e.getMessage());
      return TaskExecutionResult.failure(e.getMessage());
    }
  }
}
