package site.icebang.domain.workflow.runner;

import com.fasterxml.jackson.databind.node.ObjectNode;

import site.icebang.domain.workflow.model.TaskRun;
import site.icebang.domain.workflow.model.Task;

/** 워크플로우의 개별 Task를 실행하는 모든 Runner가 구현해야 할 인터페이스 */
public interface TaskRunner {

  /** Task 실행 결과를 담는 Record. status: SUCCESS 또는 FAILED message: 실행 결과 또는 에러 메시지 */
  record TaskExecutionResult(String status, String message) {
    public static TaskExecutionResult success(String message) {
      return new TaskExecutionResult("SUCCESS", message);
    }

    public static TaskExecutionResult failure(String message) {
      return new TaskExecutionResult("FAILED", message);
    }

    public boolean isFailure() {
      return "FAILED".equals(this.status);
    }
  }

  /**
   * 특정 Task를 실행합니다.
   *
   * @param task 실행할 Task의 정적 정의
   * @param taskRun 현재 실행에 대한 기록 객체
   * @param requestBody 동적으로 생성된 요청 데이터
   * @return Task 실행 결과
   */
  TaskExecutionResult execute(Task task, TaskRun taskRun, ObjectNode requestBody);
}
