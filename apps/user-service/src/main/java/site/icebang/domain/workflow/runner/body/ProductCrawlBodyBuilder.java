package site.icebang.domain.workflow.runner.body;

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
public class ProductCrawlBodyBuilder implements TaskBodyBuilder {

  private final ObjectMapper objectMapper;
  private static final String TASK_NAME = "상품 정보 크롤링 태스크";
  private static final String SIMILARITY_SOURCE_TASK = "상품 유사도 분석 태스크";

  @Override
  public boolean supports(String taskName) {
    return TASK_NAME.equals(taskName);
  }

  @Override
  public ObjectNode build(Task task, Map<String, JsonNode> workflowContext) {
    ObjectNode body = objectMapper.createObjectNode();

    // 유사도 분석에서 선택된 상품의 URL 가져오기
    Optional.ofNullable(workflowContext.get(SIMILARITY_SOURCE_TASK))
        .map(node -> node.path("data").path("selected_product").path("url"))
        .filter(urlNode -> !urlNode.isMissingNode() && !urlNode.asText().isEmpty())
        .ifPresent(urlNode -> body.set("product_url", urlNode));

    return body;
  }
}
