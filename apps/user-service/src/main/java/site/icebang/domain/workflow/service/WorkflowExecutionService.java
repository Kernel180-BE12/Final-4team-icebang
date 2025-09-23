package site.icebang.domain.workflow.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private final Map<String, TaskRunner> taskRunners;
  private final ObjectMapper objectMapper;
  private final List<TaskBodyBuilder> bodyBuilders;
  private final ExecutionMdcManager mdcManager;

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

    // execution_order null 처리 및 중복 처리
    taskDtos.sort(
        Comparator.comparing(
                TaskDto::getExecutionOrder, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(TaskDto::getId));

    workflowLogger.info(
        "Job (JobRunId={}) 내 총 {}개의 Task를 execution_order 순으로 실행합니다.",
        jobRun.getId(),
        taskDtos.size());

    for (TaskDto taskDto : taskDtos) {
      TaskRun taskRun = TaskRun.start(jobRun.getId(), taskDto.getId(), taskDto.getExecutionOrder());
      taskRunMapper.insert(taskRun);

      // Task 컨텍스트로 전환
      mdcManager.setTaskContext(taskRun.getId());
      workflowLogger.info(
          "Task 실행 시작: TaskId={}, ExecutionOrder={}, TaskName={}, TaskRunId={}",
          taskDto.getId(),
          taskDto.getExecutionOrder(),
          taskDto.getName(),
          taskRun.getId());

      String runnerBeanName = taskDto.getType().toLowerCase() + "TaskRunner";
      TaskRunner runner = taskRunners.get(runnerBeanName);

      if (runner == null) {
        taskRun.finish("FAILED", "지원하지 않는 Task 타입: " + taskDto.getType());
        taskRunMapper.update(taskRun);
        workflowLogger.error(
            "Task 실행 실패 (미지원 타입): Type={}, ExecutionOrder={}",
            taskDto.getType(),
            taskDto.getExecutionOrder());
        mdcManager.setJobContext(jobRun.getId()); // Job 컨텍스트로 복원
        return false;
      }

      // TaskDto에서 직접 Task 생성 (불필요한 변환 제거)
      ObjectNode requestBody =
          bodyBuilders.stream()
              .filter(builder -> builder.supports(taskDto.getName()))
              .findFirst()
              .map(builder -> builder.build(createTaskFromDto(taskDto), workflowContext))
              .orElse(objectMapper.createObjectNode());

      TaskRunner.TaskExecutionResult result =
          runner.execute(createTaskFromDto(taskDto), taskRun, requestBody);
      taskRun.finish(result.status(), result.message());
      taskRunMapper.update(taskRun);

      if (result.isFailure()) {
        workflowLogger.error(
            "Task 실행 실패: ExecutionOrder={}, Message={}",
            taskDto.getExecutionOrder(),
            result.message());
        mdcManager.setJobContext(jobRun.getId()); // Job 컨텍스트로 복원
        return false;
      }

      try {
        JsonNode resultJson = objectMapper.readTree(result.message());
        workflowContext.put(taskDto.getName(), resultJson);
      } catch (JsonProcessingException e) {
        workflowLogger.error("Task 결과 JSON 파싱 실패: ExecutionOrder={}", taskDto.getExecutionOrder());
        taskRun.finish("FAILED", "결과 JSON 파싱 실패");
        taskRunMapper.update(taskRun);
        mdcManager.setJobContext(jobRun.getId()); // Job 컨텍스트로 복원
        return false;
      }

      workflowLogger.info(
          "Task 실행 성공: ExecutionOrder={}, TaskRunId={}",
          taskDto.getExecutionOrder(),
          taskRun.getId());

      // 다시 Job 컨텍스트로 복원
      mdcManager.setJobContext(jobRun.getId());
    }
    return true;
  }

  /** TaskDto를 Task 모델로 변환합니다. 📌 주의: Reflection을 사용한 방식은 성능이 느리고 불안정하므로 권장되지 않습니다. */
  private Task createTaskFromDto(TaskDto taskDto) {
    return new Task(taskDto); // 생성자 사용
  }
}
