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
public class S3UploadBodyBuilder implements TaskBodyBuilder {

  private final ObjectMapper objectMapper;
  private static final String TASK_NAME = "S3 업로드 태스크";
  private static final String KEYWORD_SOURCE_TASK = "키워드 검색 태스크";
  private static final String CRAWL_SOURCE_TASK = "상품 정보 크롤링 태스크";

  @Override
  public boolean supports(String taskName) {
    return TASK_NAME.equals(taskName);
  }

  @Override
  public ObjectNode build(Task task, Map<String, JsonNode> workflowContext) {
    ObjectNode body = objectMapper.createObjectNode();

    // 키워드 정보 가져오기 (폴더명 생성용 - 스키마 주석 참조)
    Optional.ofNullable(workflowContext.get(KEYWORD_SOURCE_TASK))
        .map(node -> node.path("data").path("keyword"))
        .filter(node -> !node.isMissingNode() && !node.asText().trim().isEmpty())
        .ifPresent(keywordNode -> body.set("keyword", keywordNode));

    // 크롤링된 상품 데이터 가져오기
    Optional.ofNullable(workflowContext.get(CRAWL_SOURCE_TASK))
        .map(node -> node.path("data").path("crawled_products"))
        .filter(node -> !node.isMissingNode())
        .ifPresent(crawledProductsNode -> body.set("crawled_products", crawledProductsNode));

    // 기본 폴더 설정 (스키마의 기본값과 일치)
    body.put("base_folder", "product");

    return body;
  }
}
