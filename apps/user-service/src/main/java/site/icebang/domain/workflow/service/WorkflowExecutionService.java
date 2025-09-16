package site.icebang.domain.workflow.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import site.icebang.domain.workflow.mapper.JobMapper;
import site.icebang.domain.workflow.mapper.execution.JobRunMapper;
import site.icebang.domain.workflow.mapper.execution.TaskRunMapper;
import site.icebang.domain.workflow.mapper.execution.WorkflowRunMapper;
import site.icebang.domain.workflow.model.Job;
import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.model.execution.JobRun;
import site.icebang.domain.workflow.model.execution.TaskRun;
import site.icebang.domain.workflow.model.execution.WorkflowRun;
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

  /**
   * 워크플로우 실행의 시작점. 전체 과정은 하나의 트랜잭션으로 묶입니다.
   *
   * @param workflowId 실행할 워크플로우의 ID
   */
  @Transactional
  public void executeWorkflow(Long workflowId) {
    log.info("========== 워크플로우 실행 시작: WorkflowId={} ==========", workflowId);
    WorkflowRun workflowRun = WorkflowRun.start(workflowId);
    workflowRunMapper.insert(workflowRun);

    List<Job> jobs = jobMapper.findJobsByWorkflowId(workflowId);
    log.info("총 {}개의 Job을 순차적으로 실행합니다.", jobs.size());

    for (Job job : jobs) {
      JobRun jobRun = JobRun.start(workflowRun.getId(), job.getId());
      jobRunMapper.insert(jobRun);
      log.info(
          "---------- Job 실행 시작: JobId={}, JobRunId={} ----------", job.getId(), jobRun.getId());

      boolean jobSucceeded = executeTasksForJob(jobRun);

      jobRun.finish(jobSucceeded ? "SUCCESS" : "FAILED");
      jobRunMapper.update(jobRun);

      if (!jobSucceeded) {
        workflowRun.finish("FAILED");
        workflowRunMapper.update(workflowRun);
        log.error("Job 실패로 인해 워크플로우 실행을 중단합니다: WorkflowRunId={}", workflowRun.getId());
        return; // Job이 실패하면 전체 워크플로우를 중단
      }
      log.info("---------- Job 실행 성공: JobRunId={} ----------", jobRun.getId());
    }

    workflowRun.finish("SUCCESS");
    workflowRunMapper.update(workflowRun);
    log.info("========== 워크플로우 실행 성공: WorkflowRunId={} ==========", workflowRun.getId());
  }

  /**
   * 특정 Job에 속한 Task들을 순차적으로 실행합니다.
   *
   * @param jobRun 실행중인 Job의 기록 객체
   * @return 모든 Task가 성공하면 true, 하나라도 실패하면 false
   */
  private boolean executeTasksForJob(JobRun jobRun) {
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
        return false; // 실행할 Runner가 없으므로 실패
      }

      TaskRunner.TaskExecutionResult result = runner.execute(task, taskRun);
      taskRun.finish(result.status(), result.message());
      taskRunMapper.update(taskRun);

      if (result.isFailure()) {
        log.error("Task 실행 실패: TaskRunId={}, Message={}", taskRun.getId(), result.message());
        return false; // Task가 실패하면 즉시 중단하고 실패 반환
      }
      log.info("Task 실행 성공: TaskRunId={}", taskRun.getId());
    }

    return true; // 모든 Task가 성공적으로 완료됨
  }
}
