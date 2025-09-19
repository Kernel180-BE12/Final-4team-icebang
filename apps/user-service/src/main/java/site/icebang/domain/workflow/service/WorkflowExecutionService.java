package site.icebang.domain.workflow.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import site.icebang.domain.workflow.mapper.JobMapper;
import site.icebang.domain.workflow.model.Job;
import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.runner.TaskRunner;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {

  private final JobMapper jobMapper;
  private final WorkflowRunMapper workflowRunMapper;
  private final JobRunMapper jobRunMapper;
  private final TaskRunMapper taskRunMapper;
  private final Map<String, TaskRunner> taskRunners;
  private final ObjectMapper objectMapper; // 📌 JSON 처리를 위해 ObjectMapper 주입

  @Transactional
  public void executeWorkflow(Long workflowId) {
    log.info("========== 워크플로우 실행 시작: WorkflowId={} ==========", workflowId);
    WorkflowRun workflowRun = WorkflowRun.start(workflowId);
    workflowRunMapper.insert(workflowRun);

    // 📌 1. 워크플로우 전체 실행 동안 데이터를 공유할 컨텍스트 생성
    Map<String, JsonNode> workflowContext = new HashMap<>();

    List<Job> jobs = jobMapper.findJobsByWorkflowId(workflowId);
    log.info("총 {}개의 Job을 순차적으로 실행합니다.", jobs.size());

    for (Job job : jobs) {
      JobRun jobRun = JobRun.start(workflowRun.getId(), job.getId());
      jobRunMapper.insert(jobRun);
      log.info(
          "---------- Job 실행 시작: JobId={}, JobRunId={} ----------", job.getId(), jobRun.getId());

      // 📌 2. Job 내의 Task들을 실행하고, 컨텍스트를 전달하여 데이터 파이프라이닝 수행
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
    List<Task> tasks = jobMapper.findTasksByJobId(jobRun.getJobId());
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

      // 📌 3. Task 실행 전, 컨텍스트를 이용해 동적으로 Request Body를 생성
      ObjectNode requestBody = prepareRequestBody(task, workflowContext);

      // 📌 4. 동적으로 생성된 Request Body를 전달하여 Task 실행
      TaskRunner.TaskExecutionResult result = runner.execute(task, taskRun, requestBody);
      taskRun.finish(result.status(), result.message());
      taskRunMapper.update(taskRun);

      if (result.isFailure()) {
        log.error("Task 실행 실패: TaskRunId={}, Message={}", taskRun.getId(), result.message());
        return false;
      }

      // 📌 5. 성공한 Task의 결과를 다음 Task가 사용할 수 있도록 컨텍스트에 저장
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

  /** 워크플로우 컨텍스트와 Task의 input_mapping 설정을 기반으로 API 요청에 사용할 동적인 Request Body를 생성합니다. */
  private ObjectNode prepareRequestBody(Task task, Map<String, JsonNode> context) {
    ObjectNode requestBody = objectMapper.createObjectNode();
    JsonNode params = task.getParameters();
    if (params == null) return requestBody;

    JsonNode mappingRules = params.get("input_mapping");
    JsonNode staticBody = params.get("body");

    // 정적 body가 있으면 우선적으로 복사
    if (staticBody != null && staticBody.isObject()) {
      requestBody.setAll((ObjectNode) staticBody);
    }

    // input_mapping 규칙에 따라 동적으로 값 덮어쓰기/추가
    if (mappingRules != null && mappingRules.isObject()) {
      mappingRules
          .fields()
          .forEachRemaining(
              entry -> {
                String targetField = entry.getKey(); // 예: "keyword"
                String sourcePath = entry.getValue().asText(); // 예: "키워드 검색 태스크.keyword"

                String[] parts = sourcePath.split("\\.", 2);
                if (parts.length == 2) {
                  String sourceTaskName = parts[0];
                  String sourceFieldPath = parts[1];

                  JsonNode sourceData = context.get(sourceTaskName);
                  if (sourceData != null) {
                    JsonNode valueToSet = sourceData.at("/" + sourceFieldPath.replace('.', '/'));
                    if (!valueToSet.isMissingNode()) {
                      requestBody.set(targetField, valueToSet);
                    }
                  }
                }
              });
    }
    return requestBody;
  }
}
