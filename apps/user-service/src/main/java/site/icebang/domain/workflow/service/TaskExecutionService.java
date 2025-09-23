package site.icebang.domain.workflow.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import site.icebang.domain.workflow.model.TaskRun;
import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.runner.TaskRunner;

import java.util.Map;

/**
 * 워크플로우 내 개별 Task의 실행과 재시도 정책을 전담하는 서비스입니다.
 *
 * <p>이 클래스는 {@code WorkflowExecutionService}로부터 Task 실행 책임을 위임받습니다.
 * Spring AOP의 '자기 호출(Self-invocation)' 문제를 회피하고, 재시도 로직을
 * 비즈니스 흐름과 분리하기 위해 별도의 서비스로 구현되었습니다.
 *
 * <h2>주요 기능:</h2>
 * <ul>
 * <li>{@code @Retryable} 어노테이션을 통한 선언적 재시도 처리</li>
 * <li>{@code @Recover} 어노테이션을 이용한 최종 실패 시 복구 로직 수행</li>
 * <li>Task 타입에 맞는 적절한 {@code TaskRunner} 선택 및 실행</li>
 * </ul>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
@Service
@RequiredArgsConstructor
public class TaskExecutionService {
  /** 워크플로우 실행 이력 전용 로거 */
  private static final Logger workflowLogger = LoggerFactory.getLogger("WORKFLOW_HISTORY");
  private final Map<String, TaskRunner> taskRunners;

  /**
   * 지정된 Task를 재시도 정책을 적용하여 실행합니다.
   *
   * <p>HTTP 통신 오류 등 {@code RestClientException} 발생 시, 5초의 고정된 간격({@code Backoff})으로
   * 최대 3회({@code maxAttempts})까지 실행을 재시도합니다.
   * 지원하지 않는 Task 타입의 경우 재시도 없이 즉시 {@code IllegalArgumentException}을 발생시킵니다.
   *
   * @param task        실행할 Task의 도메인 모델
   * @param taskRun     현재 실행에 대한 기록 객체
   * @param requestBody 동적으로 생성된 요청 Body
   * @return Task 실행 결과
   * @throws IllegalArgumentException 지원하지 않는 Task 타입일 경우
   * @since v0.1.0
   */
  @Retryable(
          value = { RestClientException.class },
          maxAttempts = 3,
          backoff = @Backoff(delay = 5000)
  )
  public TaskRunner.TaskExecutionResult executeWithRetry(Task task, TaskRun taskRun, ObjectNode requestBody) {
    workflowLogger.info("Task 실행 시도: TaskId={}, TaskRunId={}", task.getId(), taskRun.getId());

    String runnerBeanName = task.getType().toLowerCase() + "TaskRunner";
    TaskRunner runner = taskRunners.get(runnerBeanName);

    if (runner == null) {
      throw new IllegalArgumentException("지원하지 않는 Task 타입: " + task.getType());
    }

    return runner.execute(task, taskRun, requestBody);
  }

  /**
   * {@code @Retryable} 재시도가 모두 실패했을 때 호출되는 복구 메소드입니다.
   *
   * <p>이 메소드는 {@code executeWithRetry} 메소드와 동일한 파라미터 시그니처를 가지며,
   * 발생한 예외를 첫 번째 파라미터로 추가로 받습니다. 최종 실패 상태를 기록하고
   * 실패 결과를 반환하는 역할을 합니다.
   *
   * @param e           재시도를 유발한 마지막 예외 객체
   * @param task        실패한 Task의 도메인 모델
   * @param taskRun     실패한 실행의 기록 객체
   * @param requestBody 실패 당시 사용된 요청 Body
   * @return 최종 실패를 나타내는 Task 실행 결과
   * @since v0.1.0
   */
  @Recover
  public TaskRunner.TaskExecutionResult recover(RestClientException e, Task task, TaskRun taskRun, ObjectNode requestBody) {
    workflowLogger.error("최종 Task 실행 실패 (모든 재시도 소진): TaskRunId={}", taskRun.getId(), e);
    return TaskRunner.TaskExecutionResult.failure("최대 재시도 횟수 초과: " + e.getMessage());
  }
}