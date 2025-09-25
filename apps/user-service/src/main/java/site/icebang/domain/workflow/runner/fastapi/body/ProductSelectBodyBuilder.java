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
public class ProductSelectBodyBuilder implements TaskBodyBuilder {

    private final ObjectMapper objectMapper;
    private static final String TASK_NAME = "상품 선택 태스크";

    @Override
    public boolean supports(String taskName) {
        return TASK_NAME.equals(taskName);
    }

    @Override
    public ObjectNode build(Task task, Map<String, JsonNode> workflowContext) {
        ObjectNode body = objectMapper.createObjectNode();

        // task_run_id는 현재 실행 중인 task의 run_id를 사용
        // 실제 구현에서는 Task 객체나 워크플로우 컨텍스트에서 가져와야 할 수 있습니다.
        body.put("task_run_id", task.getId()); // Task 객체에서 ID를 가져오는 방식으로 가정

        // 기본 선택 기준 설정 (이미지 개수 우선)
        body.put("selection_criteria", "image_count_priority");

        return body;
    }
}