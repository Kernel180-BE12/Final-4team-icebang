package site.icebang.domain.workflow.runner.body;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.icebang.domain.workflow.model.Task;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EmptyBodyBuilder implements TaskBodyBuilder {

    private final ObjectMapper objectMapper;
    private static final Set<String> SUPPORTED_TASKS = Set.of(
            "상품 유사도 분석 태스크",
            "상품 정보 크롤링 태스크",
            "블로그 RAG 생성 태스크",
            "블로그 발행 태스크"
    );

    @Override
    public boolean supports(String taskName) {
        return SUPPORTED_TASKS.contains(taskName);
    }

    @Override
    public ObjectNode build(Task task, Map<String, JsonNode> workflowContext) {
        // 이 Task들은 Body가 필요 없으므로 빈 객체를 반환합니다.
        // TODO: 나중에 이 Task들이 이전 단계의 결과값을 필요로 하게 되면,
        //       다른 빌더들처럼 workflowContext에서 데이터를 꺼내 Body를 구성하도록 수정합니다.
        return objectMapper.createObjectNode();
    }
}