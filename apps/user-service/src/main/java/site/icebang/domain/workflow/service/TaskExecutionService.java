package site.icebang.domain.workflow.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.model.TaskRun;
import site.icebang.domain.workflow.runner.TaskRunner;

@Service
@RequiredArgsConstructor
public class TaskExecutionService {
  private static final Logger workflowLogger = LoggerFactory.getLogger("WORKFLOW_HISTORY");
  private final Map<String, TaskRunner> taskRunners;
  private final RetryTemplate taskExecutionRetryTemplate; // 📌 RetryTemplate 주입

  // 📌 @Retryable, @Recover 어노테이션 제거
  public TaskRunner.TaskExecutionResult executeWithRetry(
      Task task, TaskRun taskRun, ObjectNode requestBody) {

    // RetryTemplate을 사용하여 실행 로직을 감쌉니다.
    return taskExecutionRetryTemplate.execute(
        // 1. 재시도할 로직 (RetryCallback)
        context -> {
          // 📌 이 블록은 재시도할 때마다 실행되므로, 로그가 누락되지 않습니다.
          workflowLogger.info(
              "Task 실행 시도 #{}: TaskId={}, TaskRunId={}",
              context.getRetryCount() + 1,
              task.getId(),
              taskRun.getId());

          String runnerBeanName = task.getType().toLowerCase() + "TaskRunner";
          TaskRunner runner = taskRunners.get(runnerBeanName);

          if (runner == null) {
            throw new IllegalArgumentException("지원하지 않는 Task 타입: " + task.getType());
          }

          // 이 부분에서 RestClientException 발생 시 재시도됩니다.
          return runner.execute(task, taskRun, requestBody);
        },
        // 2. 모든 재시도가 실패했을 때 실행될 로직 (RecoveryCallback)
        context -> {
          Throwable lastThrowable = context.getLastThrowable();
          workflowLogger.error(
              "최종 Task 실행 실패 (모든 재시도 소진): TaskRunId={}", taskRun.getId(), lastThrowable);
          return TaskRunner.TaskExecutionResult.failure(
              "최대 재시도 횟수 초과: " + lastThrowable.getMessage());
        });
  }
}
