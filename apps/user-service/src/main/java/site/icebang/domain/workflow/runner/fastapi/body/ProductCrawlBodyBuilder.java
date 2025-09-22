package site.icebang.domain.workflow.runner.fastapi.body;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ArrayNode;
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

        // ArrayNode 준비 (product_urls 배열로 변경)
        ArrayNode productUrls = objectMapper.createArrayNode();

        // 유사도 분석에서 선택된 상품들의 URL 가져오기 (복수로 변경)
        Optional.ofNullable(workflowContext.get(SIMILARITY_SOURCE_TASK))
                .ifPresent(node -> {
                    JsonNode topProducts = node.path("data").path("top_products");
                    if (topProducts.isArray()) {
                        // top_products 배열에서 각 상품의 URL 추출
                        topProducts.forEach(product -> {
                            JsonNode urlNode = product.path("url");
                            if (!urlNode.isMissingNode() && !urlNode.asText().isEmpty()) {
                                productUrls.add(urlNode.asText());
                            }
                        });
                    }
                });

        body.set("product_urls", productUrls);

        return body;
    }
}