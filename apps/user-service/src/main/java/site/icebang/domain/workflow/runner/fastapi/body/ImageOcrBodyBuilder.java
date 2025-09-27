package site.icebang.domain.workflow.runner.fastapi.body;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.workflow.model.Task;

@Component
@RequiredArgsConstructor
public class ImageOcrBodyBuilder implements TaskBodyBuilder {

  private final ObjectMapper objectMapper;
  private static final String TASK_NAME = "이미지 OCR 태스크";
  private static final String KEYWORD_SOURCE_TASK = "키워드 검색 태스크";

  @Override
  public boolean supports(String taskName) {
    return TASK_NAME.equals(taskName);
  }

  @Override
  public ObjectNode build(Task task, Map<String, JsonNode> workflowContext) {
    ObjectNode body = objectMapper.createObjectNode();

    // 키워드 정보 가져오기 (OCR 처리용)
    Optional.ofNullable(workflowContext.get(KEYWORD_SOURCE_TASK))
        .map(node -> node.path("data").path("keyword"))
        .filter(node -> !node.isMissingNode() && !node.asText().trim().isEmpty())
        .ifPresent(keywordNode -> body.set("keyword", keywordNode));

    return body;
  }
}
