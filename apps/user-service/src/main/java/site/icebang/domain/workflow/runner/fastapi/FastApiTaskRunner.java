package site.icebang.domain.workflow.runner.fastapi;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.model.TaskRun;
import site.icebang.domain.workflow.runner.TaskRunner;
import site.icebang.external.fastapi.adapter.FastApiAdapter;

/**
 * FastAPI 서버와 통신하는 Task를 실행하는 구체적인 Runner 구현체입니다.
 *
 * <p>이 클래스는 {@code TaskRunner} 인터페이스를 구현하며, Task의 타입이 'FastAPI'일 때 선택됩니다. 실제 HTTP 통신은 {@code
 * FastApiAdapter}에 위임하고, 이 클래스는 워크플로우의 {@code Task} 객체를 {@code FastApiAdapter}가 이해할 수 있는 호출 형식으로
 * 변환하는 **어댑터(Adapter)** 역할을 수행합니다.
 *
 * <h2>주요 기능:</h2>
 *
 * <ul>
 *   <li>Task 파라미터에서 endpoint와 method 정보 파싱
 *   <li>사전에 생성된 Request Body를 {@code FastApiAdapter}에 전달하여 실행 위임
 *   <li>어댑터의 실행 결과를 {@code TaskExecutionResult} 형식으로 변환하여 반환
 * </ul>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
@Component("fastapiTaskRunner")
@RequiredArgsConstructor
public class FastApiTaskRunner implements TaskRunner {

  /** FastAPI 서버와의 통신을 전담하는 어댑터 */
  private final FastApiAdapter fastApiAdapter;

  /**
   * FastAPI 타입의 Task를 실행합니다.
   *
   * <p>Task의 파라미터에서 엔드포인트와 HTTP 메소드를 추출하고, {@code WorkflowExecutionService}로부터 전달받은 동적 Request
   * Body를 사용하여 {@code FastApiAdapter}를 호출합니다.
   *
   * @param task 실행할 Task의 정적 정의
   * @param taskRun 현재 실행에 대한 기록 객체
   * @param requestBody {@code TaskBodyBuilder}에 의해 동적으로 생성된 최종 요청 Body
   * @return {@code FastApiAdapter}의 호출 결과를 담은 {@code TaskExecutionResult} 객체
   * @since v0.1.0
   */
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
