package site.icebang.domain.workflow.runner.fastapi.body;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.workflow.model.Task;

@Component
@RequiredArgsConstructor
public class ProductSearchBodyBuilder implements TaskBodyBuilder {

  private final ObjectMapper objectMapper;
  private static final String TASK_NAME = "상품 검색 태스크";
  private static final String SOURCE_TASK_NAME = "키워드 검색 태스크";

  @Override
  public boolean supports(String taskName) {
    return TASK_NAME.equals(taskName);
  }

  @Override
  public ObjectNode build(Task task, Map<String, JsonNode> workflowContext) {
    JsonNode sourceResult = workflowContext.get(SOURCE_TASK_NAME);
    String keyword =
        sourceResult != null ? sourceResult.path("data").path("keyword").asText("") : "";
    return objectMapper.createObjectNode().put("keyword", keyword);
  }
}
