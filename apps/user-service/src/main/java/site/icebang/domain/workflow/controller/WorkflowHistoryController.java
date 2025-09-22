package site.icebang.domain.workflow.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.ApiResponse;

@RestController
@RequestMapping("/v0/workflow-runs")
@RequiredArgsConstructor
public class WorkflowHistoryController {
  @GetMapping("/{runId}")
  public ApiResponse<Map<String, Object>> getWorkflowRunDetail(@PathVariable Long runId) {
    Map<String, Object> response = new HashMap<>();

    // Workflow Run
    Map<String, Object> workflowRun = new HashMap<>();
    workflowRun.put("id", 1L);
    workflowRun.put("workflowId", 1L);
    workflowRun.put("workflowName", "네이버 블로그 포스팅#1");
    workflowRun.put("workflowDescription", "네이버 트렌드 기반 자동 블로그 포스팅 워크플로");
    workflowRun.put("runNumber", "RUN-2024090100001");
    workflowRun.put("status", "success");
    workflowRun.put("triggerType", "scheduled");
    workflowRun.put("startedAt", "2024-09-01T09:00:00Z");
    workflowRun.put("finishedAt", "2024-09-01T09:08:20Z");
    workflowRun.put("durationMs", 500000);
    workflowRun.put("createdBy", 1L);
    workflowRun.put("createdAt", "2024-09-01T09:00:00Z");

    // Task Runs for Job 1
    List<Map<String, Object>> taskRuns1 =
        Arrays.asList(
            createTaskRun(
                1L,
                1L,
                1L,
                "네이버 트렌드 크롤링",
                "실시간 네이버 트렌드 키워드를 수집하고 분석합니다",
                "FastAPI",
                "success",
                1,
                "2024-09-01T09:00:00Z",
                "2024-09-01T09:02:15Z",
                135000),
            createTaskRun(
                2L,
                1L,
                2L,
                "싸다구 몰 검색",
                "수집된 트렌드 키워드로 싸다구 몰에서 관련 상품을 검색합니다",
                "FastAPI",
                "success",
                2,
                "2024-09-01T09:02:15Z",
                "2024-09-01T09:03:45Z",
                90000),
            createTaskRun(
                3L,
                1L,
                5L,
                "상품 정보 추출",
                "검색된 상품들의 상세 정보를 추출하고 검증합니다",
                "FastAPI",
                "success",
                3,
                "2024-09-01T09:03:45Z",
                "2024-09-01T09:04:30Z",
                45000));

    // Task Runs for Job 2
    List<Map<String, Object>> taskRuns2 =
        Arrays.asList(
            createTaskRun(
                4L,
                2L,
                6L,
                "콘텐츠 생성",
                "AI를 활용하여 상품 정보 기반의 블로그 콘텐츠를 생성합니다",
                "FastAPI",
                "success",
                1,
                "2024-09-01T09:04:30Z",
                "2024-09-01T09:07:50Z",
                200000),
            createTaskRun(
                5L,
                2L,
                7L,
                "블로그 업로드",
                "생성된 콘텐츠를 지정된 블로그 플랫폼에 업로드합니다",
                "FastAPI",
                "success",
                2,
                "2024-09-01T09:07:50Z",
                "2024-09-01T09:08:20Z",
                30000));

    // Job Runs
    List<Map<String, Object>> jobRuns =
        Arrays.asList(
            createJobRun(
                1L,
                1L,
                1L,
                "상품 분석",
                "키워드 검색, 상품 크롤링 및 유사도 분석 작업",
                "success",
                1,
                "2024-09-01T09:00:00Z",
                "2024-09-01T09:04:30Z",
                270000,
                taskRuns1),
            createJobRun(
                2L,
                1L,
                2L,
                "블로그 콘텐츠 생성",
                "분석 데이터를 기반으로 RAG 콘텐츠 생성 및 발행 작업",
                "success",
                2,
                "2024-09-01T09:04:30Z",
                "2024-09-01T09:08:20Z",
                230000,
                taskRuns2));

    response.put("traceId", "550e8400-e29b-41d4-a716-446655440000");
    response.put("workflowRun", workflowRun);
    response.put("jobRuns", jobRuns);

    return ApiResponse.success(response);
  }

  private Map<String, Object> createTaskRun(
      Long id,
      Long jobRunId,
      Long taskId,
      String taskName,
      String taskDescription,
      String taskType,
      String status,
      int executionOrder,
      String startedAt,
      String finishedAt,
      Integer durationMs) {
    Map<String, Object> taskRun = new HashMap<>();
    taskRun.put("id", id);
    taskRun.put("jobRunId", jobRunId);
    taskRun.put("taskId", taskId);
    taskRun.put("taskName", taskName);
    taskRun.put("taskDescription", taskDescription);
    taskRun.put("taskType", taskType);
    taskRun.put("status", status);
    taskRun.put("executionOrder", executionOrder);
    taskRun.put("startedAt", startedAt);
    taskRun.put("finishedAt", finishedAt);
    taskRun.put("durationMs", durationMs);
    return taskRun;
  }

  private Map<String, Object> createJobRun(
      Long id,
      Long workflowRunId,
      Long jobId,
      String jobName,
      String jobDescription,
      String status,
      int executionOrder,
      String startedAt,
      String finishedAt,
      Integer durationMs,
      List<Map<String, Object>> taskRuns) {
    Map<String, Object> jobRun = new HashMap<>();
    jobRun.put("id", id);
    jobRun.put("workflowRunId", workflowRunId);
    jobRun.put("jobId", jobId);
    jobRun.put("jobName", jobName);
    jobRun.put("jobDescription", jobDescription);
    jobRun.put("status", status);
    jobRun.put("executionOrder", executionOrder);
    jobRun.put("startedAt", startedAt);
    jobRun.put("finishedAt", finishedAt);
    jobRun.put("durationMs", durationMs);
    jobRun.put("taskRuns", taskRuns);
    return jobRun;
  }

  private Map<String, Object> createLog(
      Long id,
      String executionType,
      Long sourceId,
      Long runId,
      String logLevel,
      String status,
      String logMessage,
      String executedAt,
      Integer durationMs) {
    Map<String, Object> log = new HashMap<>();
    log.put("id", id);
    log.put("executionType", executionType);
    log.put("sourceId", sourceId);
    log.put("runId", runId);
    log.put("logLevel", logLevel);
    log.put("status", status);
    log.put("logMessage", logMessage);
    log.put("executedAt", executedAt);
    log.put("durationMs", durationMs);
    return log;
  }
}
