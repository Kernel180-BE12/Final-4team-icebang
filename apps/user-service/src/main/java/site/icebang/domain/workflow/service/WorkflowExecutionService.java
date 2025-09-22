package site.icebang.domain.workflow.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import site.icebang.domain.execution.mapper.JobRunMapper;
import site.icebang.domain.execution.mapper.TaskRunMapper;
import site.icebang.domain.execution.mapper.WorkflowRunMapper;
import site.icebang.domain.execution.model.JobRun;
import site.icebang.domain.execution.model.TaskRun;
import site.icebang.domain.execution.model.WorkflowRun;
import site.icebang.domain.workflow.dto.TaskDto;
import site.icebang.domain.workflow.manager.ExecutionMdcManager;
import site.icebang.domain.workflow.mapper.JobMapper;
import site.icebang.domain.workflow.model.Job;
import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.runner.TaskRunner;
import site.icebang.domain.workflow.runner.fastapi.body.TaskBodyBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {
  private static final Logger workflowLogger = LoggerFactory.getLogger("WORKFLOW_HISTORY");
  private final JobMapper jobMapper;
  private final WorkflowRunMapper workflowRunMapper;
  private final JobRunMapper jobRunMapper;
  private final TaskRunMapper taskRunMapper;
  private final ObjectMapper objectMapper;
  private final List<TaskBodyBuilder> bodyBuilders;
  private final ExecutionMdcManager mdcManager;
  private final TaskExecutionService taskExecutionService; // 📌 재시도 전담 서비스 주입

  @Transactional
  @Async("traceExecutor")
  public void executeWorkflow(Long workflowId) {
    mdcManager.setWorkflowContext(workflowId);

    try {
      workflowLogger.info("========== 워크플로우 실행 시작: WorkflowId={} ==========", workflowId);

      WorkflowRun workflowRun = WorkflowRun.start(workflowId);
      workflowRunMapper.insert(workflowRun);

      Map<String, JsonNode> workflowContext = new HashMap<>();
      List<Job> jobs = jobMapper.findJobsByWorkflowId(workflowId);
      workflowLogger.info("총 {}개의 Job을 순차적으로 실행합니다.", jobs.size());

      for (Job job : jobs) {
        JobRun jobRun = JobRun.start(workflowRun.getId(), job.getId());
        jobRunMapper.insert(jobRun);

        // Job 컨텍스트로 전환
        mdcManager.setJobContext(jobRun.getId());
        workflowLogger.info(
            "---------- Job 실행 시작: JobId={}, JobRunId={} ----------", job.getId(), jobRun.getId());

        boolean jobSucceeded = executeTasksForJob(jobRun, workflowContext);

        jobRun.finish(jobSucceeded ? "SUCCESS" : "FAILED");
        jobRunMapper.update(jobRun);

        if (!jobSucceeded) {
          workflowRun.finish("FAILED");
          workflowRunMapper.update(workflowRun);
          workflowLogger.error("Job 실패로 인해 워크플로우 실행을 중단합니다: WorkflowRunId={}", workflowRun.getId());
          return;
        }

        workflowLogger.info("---------- Job 실행 성공: JobRunId={} ----------", jobRun.getId());

        // 다시 워크플로우 컨텍스트로 복원
        mdcManager.setWorkflowContext(workflowId);
      }

      workflowRun.finish("SUCCESS");
      workflowRunMapper.update(workflowRun);
      workflowLogger.info(
          "========== 워크플로우 실행 성공: WorkflowRunId={} ==========", workflowRun.getId());

    } finally {
      mdcManager.clearExecutionContext();
    }
  }

  private boolean executeTasksForJob(JobRun jobRun, Map<String, JsonNode> workflowContext) {
    List<TaskDto> taskDtos = jobMapper.findTasksByJobId(jobRun.getJobId());
    List<Task> tasks = taskDtos.stream().map(this::convertToTask).collect(Collectors.toList());

    workflowLogger.info("Job (JobRunId={}) 내 총 {}개의 Task를 실행합니다.", jobRun.getId(), tasks.size());

    for (Task task : tasks) {
      TaskRun taskRun = TaskRun.start(jobRun.getId(), task.getId());
      taskRunMapper.insert(taskRun);

      // Task 컨텍스트로 전환
      mdcManager.setTaskContext(taskRun.getId());
      workflowLogger.info("Task 실행 시작: TaskId={}, TaskRunId={}", task.getId(), taskRun.getId());

      ObjectNode requestBody = bodyBuilders.stream()
              .filter(builder -> builder.supports(task.getName()))
              .findFirst()
              .map(builder -> builder.build(task, workflowContext))
              .orElse(objectMapper.createObjectNode());

      // 재시도 로직이 포함된 TaskExecutionService를 호출
      TaskRunner.TaskExecutionResult result;
      try {
        result = taskExecutionService.executeWithRetry(task, taskRun, requestBody);
      } catch (IllegalArgumentException e) {
        // Runner를 찾지 못한 경우 등 재시도 대상이 아닌 예외 처리
        result = TaskRunner.TaskExecutionResult.failure(e.getMessage());
      }

      taskRun.finish(result.status(), result.message());
      taskRunMapper.update(taskRun);

      if (result.isFailure()) {
        workflowLogger.error("Task 실행 실패: Message={}", result.message());
        mdcManager.setJobContext(jobRun.getId()); // Job 컨텍스트로 복원
        return false;
      }

      try {
        JsonNode resultJson = objectMapper.readTree(result.message());
        workflowContext.put(task.getName(), resultJson);
      } catch (JsonProcessingException e) {
        workflowLogger.error("Task 결과 JSON 파싱 실패");
        taskRun.finish("FAILED", "결과 JSON 파싱 실패");
        taskRunMapper.update(taskRun);
        mdcManager.setJobContext(jobRun.getId()); // Job 컨텍스트로 복원
        return false;
      }

      workflowLogger.info("Task 실행 성공: TaskRunId={}", taskRun.getId());

      // 다시 Job 컨텍스트로 복원
      mdcManager.setJobContext(jobRun.getId());
    }
    return true;
  }

  /** TaskDto를 Task 모델로 변환합니다. 📌 주의: Reflection을 사용한 방식은 성능이 느리고 불안정하므로 권장되지 않습니다. */
  private Task convertToTask(TaskDto taskDto) {
    Task task = new Task();
    try {
      java.lang.reflect.Field idField = Task.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(task, taskDto.getId());

      java.lang.reflect.Field nameField = Task.class.getDeclaredField("name");
      nameField.setAccessible(true);
      nameField.set(task, taskDto.getName());

      java.lang.reflect.Field typeField = Task.class.getDeclaredField("type");
      typeField.setAccessible(true);
      typeField.set(task, taskDto.getType());

      java.lang.reflect.Field parametersField = Task.class.getDeclaredField("parameters");
      parametersField.setAccessible(true);
      parametersField.set(task, taskDto.getParameters());

    } catch (Exception e) {
      throw new RuntimeException("TaskDto to Task 변환 중 오류 발생", e);
    }
    return task;
  }
}
