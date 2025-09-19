package site.icebang.domain.workflow.runner.body;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import site.icebang.domain.workflow.model.Task;

public interface TaskBodyBuilder {

  /**
   * 이 빌더가 어떤 Task를 지원하는지 식별합니다.
   *
   * @param taskName Task의 고유한 이름
   * @return 지원하면 true, 아니면 false
   */
  boolean supports(String taskName);

  /**
   * 실제 API 요청에 사용될 Body를 생성합니다.
   *
   * @param task DB에 저장된 Task의 원본 정의
   * @param workflowContext 이전 Task들의 결과가 담긴 컨텍스트
   * @return 생성된 JSON Body
   */
  ObjectNode build(Task task, Map<String, JsonNode> workflowContext);
}
