package site.icebang.domain.workflow.runner;

import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.model.execution.TaskRun;

public interface TaskRunner {
    record TaskExecutionResult(String status, String message) {
        public static TaskExecutionResult success(String message) { return new TaskExecutionResult("SUCCESS", message); }
        public static TaskExecutionResult failure(String message) { return new TaskExecutionResult("FAILED", message); }
        public boolean isFailure() { return "FAILED".equals(status); }
    }

    TaskExecutionResult execute(Task task, TaskRun taskRun);
}