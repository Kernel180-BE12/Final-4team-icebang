package site.icebang.domain.workflow.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import site.icebang.domain.workflow.mapper.JobMapper;
import site.icebang.domain.workflow.model.Job;
import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.runner.TaskRunner;
import site.icebang.domain.workflow.runner.body.TaskBodyBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {

  private final JobMapper jobMapper;
  private final WorkflowRunMapper workflowRunMapper;
  private final JobRunMapper jobRunMapper;
  private final TaskRunMapper taskRunMapper;
  private final Map<String, TaskRunner> taskRunners;
  private final ObjectMapper objectMapper;
  private final List<TaskBodyBuilder> bodyBuilders;

  @Transactional
  public void executeWorkflow(Long workflowId) {
    log.info("========== 워크플로우 실행 시작: WorkflowId={} ==========", workflowId);
    WorkflowRun workflowRun = WorkflowRun.start(workflowId);
    workflowRunMapper.insert(workflowRun);

    Map<String, JsonNode> workflowContext = new HashMap<>();
    List<Job> jobs = jobMapper.findJobsByWorkflowId(workflowId);
    log.info("총 {}개의 Job을 순차적으로 실행합니다.", jobs.size());

    for (Job job : jobs) {
      JobRun jobRun = JobRun.start(workflowRun.getId(), job.getId());
      jobRunMapper.insert(jobRun);
      log.info(
          "---------- Job 실행 시작: JobId={}, JobRunId={} ----------", job.getId(), jobRun.getId());

      boolean jobSucceeded = executeTasksForJob(jobRun, workflowContext);

      jobRun.finish(jobSucceeded ? "SUCCESS" : "FAILED");
      jobRunMapper.update(jobRun);

      if (!jobSucceeded) {
        workflowRun.finish("FAILED");
        workflowRunMapper.update(workflowRun);
        log.error("Job 실패로 인해 워크플로우 실행을 중단합니다: WorkflowRunId={}", workflowRun.getId());
        return;
      }
      log.info("---------- Job 실행 성공: JobRunId={} ----------", jobRun.getId());
    }

    workflowRun.finish("SUCCESS");
    workflowRunMapper.update(workflowRun);
    log.info("========== 워크플로우 실행 성공: WorkflowRunId={} ==========", workflowRun.getId());
  }

  private boolean executeTasksForJob(JobRun jobRun, Map<String, JsonNode> workflowContext) {
    // 📌 Mapper로부터 TaskDto 리스트를 조회합니다.
    List<TaskDto> taskDtos = jobMapper.findTasksByJobId(jobRun.getJobId());

    // 📌 convertToTask 메소드를 사용하여 Task 모델 리스트로 변환합니다.
    List<Task> tasks = taskDtos.stream().map(this::convertToTask).collect(Collectors.toList());

    log.info("Job (JobRunId={}) 내 총 {}개의 Task를 실행합니다.", jobRun.getId(), tasks.size());

    for (Task task : tasks) {
      TaskRun taskRun = TaskRun.start(jobRun.getId(), task.getId());
      taskRunMapper.insert(taskRun);
      log.info("Task 실행 시작: TaskId={}, TaskRunId={}", task.getId(), taskRun.getId());

      String runnerBeanName = task.getType().toLowerCase() + "TaskRunner";
      TaskRunner runner = taskRunners.get(runnerBeanName);

      if (runner == null) {
        taskRun.finish("FAILED", "지원하지 않는 Task 타입: " + task.getType());
        taskRunMapper.update(taskRun);
        log.error("Task 실행 실패 (미지원 타입): TaskRunId={}, Type={}", taskRun.getId(), task.getType());
        return false;
      }

      ObjectNode requestBody =
          bodyBuilders.stream()
              .filter(builder -> builder.supports(task.getName()))
              .findFirst()
              .map(builder -> builder.build(task, workflowContext))
              .orElse(objectMapper.createObjectNode());

      TaskRunner.TaskExecutionResult result = runner.execute(task, taskRun, requestBody);
      taskRun.finish(result.status(), result.message());
      taskRunMapper.update(taskRun);

      if (result.isFailure()) {
        log.error("Task 실행 실패: TaskRunId={}, Message={}", taskRun.getId(), result.message());
        return false;
      }

      try {
        JsonNode resultJson = objectMapper.readTree(result.message());
        workflowContext.put(task.getName(), resultJson);
        // TODO: task_io_data 테이블에 requestBody(INPUT)와 resultJson(OUTPUT) 저장
      } catch (JsonProcessingException e) {
        log.error("Task 결과 JSON 파싱 실패: TaskRunId={}", taskRun.getId(), e);
        taskRun.finish("FAILED", "결과 JSON 파싱 실패");
        taskRunMapper.update(taskRun);
        return false;
      }
      log.info("Task 실행 성공: TaskRunId={}", taskRun.getId());
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
