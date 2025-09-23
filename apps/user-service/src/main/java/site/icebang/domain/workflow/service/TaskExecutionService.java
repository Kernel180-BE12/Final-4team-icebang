package site.icebang.domain.workflow.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskExecutionService { // 📌 클래스 이름 변경
    private static final Logger workflowLogger = LoggerFactory.getLogger("WORKFLOW_HISTORY");
    private final Map<String, TaskRunner> taskRunners;

    /**
     * RestClientException 발생 시, 5초 간격으로 최대 3번 재시도합니다.
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
     * 모든 재시도가 실패했을 때 마지막으로 호출될 복구 메소드입니다.
     */
    @Recover
    public TaskRunner.TaskExecutionResult recover(RestClientException e, Task task, TaskRun taskRun, ObjectNode requestBody) {
        workflowLogger.error("최종 Task 실행 실패 (모든 재시도 소진): TaskRunId={}", taskRun.getId(), e);
        return TaskRunner.TaskExecutionResult.failure("최대 재시도 횟수 초과: " + e.getMessage());
    }
}