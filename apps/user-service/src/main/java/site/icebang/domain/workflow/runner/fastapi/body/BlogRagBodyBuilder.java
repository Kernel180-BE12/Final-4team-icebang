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
public class BlogRagBodyBuilder implements TaskBodyBuilder {

  private final ObjectMapper objectMapper;
  private static final String TASK_NAME = "블로그 RAG 생성 태스크";
  private static final String KEYWORD_SOURCE_TASK = "키워드 검색 태스크";
  private static final String PRODUCT_SELECT_SOURCE_TASK = "상품 선택 태스크"; // 변경: S3 업로드 → 상품 선택

  @Override
  public boolean supports(String taskName) {
    return TASK_NAME.equals(taskName);
  }

  @Override
  public ObjectNode build(Task task, Map<String, JsonNode> workflowContext) {
    ObjectNode body = objectMapper.createObjectNode();

    // 키워드 정보 가져오기
    Optional.ofNullable(workflowContext.get(KEYWORD_SOURCE_TASK))
        .map(node -> node.path("data").path("keyword"))
        .ifPresent(keywordNode -> body.set("keyword", keywordNode));

    Optional.ofNullable(workflowContext.get(PRODUCT_SELECT_SOURCE_TASK))
        .map(node -> node.path("data").path("selected_product"))
        .ifPresent(productNode -> body.set("product_info", productNode));

    // 기본 콘텐츠 설정
    body.put("content_type", "review_blog");
    body.put("target_length", 1000);

    return body;
  }
}
