package site.icebang.domain.workflow.runner;

import com.fasterxml.jackson.databind.node.ObjectNode;

import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.model.TaskRun;

/**
 * 워크플로우 내 개별 Task의 실행을 담당하는 모든 Runner 객체가 구현해야 할 공통 인터페이스입니다.
 *
 * <p>이 인터페이스는 전략 패턴(Strategy Pattern)의 '전략(Strategy)' 역할을 수행합니다. {@code WorkflowExecutionService}는
 * 이 인터페이스에 의존하여, Task의 타입('FastAPI' 등)에 따라 적절한 Runner 구현체를 선택하고 실행 로직을 위임합니다.
 *
 * <h2>주요 구성 요소:</h2>
 *
 * <ul>
 *   <li><b>TaskExecutionResult</b>: 모든 Task 실행 결과가 따라야 할 표준 응답 형식을 정의하는 내부 Record
 *   <li><b>execute</b>: Task 실행을 위한 단일 추상 메소드
 * </ul>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
public interface TaskRunner {

  /**
   * Task 실행 결과를 담는 불변(Immutable) 데이터 객체(Record)입니다.
   *
   * <p>실행의 성공/실패 여부(status)와 결과 메시지(message)를 표준화된 방식으로 반환합니다.
   *
   * @param status 실행 상태 ("SUCCESS" 또는 "FAILED")
   * @param message 실행 결과 (성공 시 응답 Body, 실패 시 에러 메시지)
   * @since v0.1.0
   */
  record TaskExecutionResult(String status, String message) {
    public static TaskExecutionResult success(String message) {
      return new TaskExecutionResult("SUCCESS", message);
    }

    /**
     * 실패 결과를 생성하는 정적 팩토리 메소드입니다.
     *
     * @param message 실패 원인 메시지
     * @return status가 "FAILED"로 설정된 결과 객체
     */
    public static TaskExecutionResult failure(String message) {
      return new TaskExecutionResult("FAILED", message);
    }

    /**
     * 해당 결과가 실패했는지 여부를 반환합니다.
     *
     * @return 실패했다면 true, 아니면 false
     */
    public boolean isFailure() {
      return "FAILED".equals(this.status);
    }
  }

  /**
   * 특정 Task를 실행합니다.
   *
   * @param task 실행할 Task의 정적 정의 (이름, 타입, 파라미터 등)
   * @param taskRun 현재 실행에 대한 DB 기록 객체 (ID 추적 등에 사용)
   * @param requestBody {@code TaskBodyBuilder}에 의해 동적으로 생성된 최종 요청 Body
   * @return Task 실행 결과를 담은 {@code TaskExecutionResult} 객체
   * @since v0.1.0
   */
  TaskExecutionResult execute(Task task, TaskRun taskRun, ObjectNode requestBody);
}
