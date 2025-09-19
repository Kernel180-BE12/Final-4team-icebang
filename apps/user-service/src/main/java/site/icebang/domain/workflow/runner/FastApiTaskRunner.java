package site.icebang.domain.workflow.runner;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import site.icebang.domain.execution.model.TaskRun;
import site.icebang.domain.workflow.model.Task;
import site.icebang.external.fastapi.adapter.FastApiAdapter; // 📌 새로운 어댑터 import

@Component("fastapiTaskRunner")
@RequiredArgsConstructor
public class FastApiTaskRunner implements TaskRunner {

  private final FastApiAdapter fastApiAdapter;

  @Override
  public TaskExecutionResult execute(Task task, TaskRun taskRun, ObjectNode requestBody) {
    JsonNode params = task.getParameters();
    String endpoint = params.path("endpoint").asText();
    HttpMethod method = HttpMethod.valueOf(params.path("method").asText("POST").toUpperCase());

    String responseBody = fastApiAdapter.call(endpoint, method, requestBody.toString());

    if (responseBody == null) {
      return TaskExecutionResult.failure("FastApiAdapter 호출에 실패했습니다.");
    }
    return TaskExecutionResult.success(responseBody);
  }
}