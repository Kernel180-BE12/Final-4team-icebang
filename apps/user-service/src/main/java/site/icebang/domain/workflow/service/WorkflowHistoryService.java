package site.icebang.domain.workflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.common.service.PageableService;
import site.icebang.domain.workflow.dto.*;
import site.icebang.domain.workflow.mapper.WorkflowHistoryMapper;

@Service
@RequiredArgsConstructor
public class WorkflowHistoryService implements PageableService<WorkflowHistoryDTO> {
  private final WorkflowHistoryMapper workflowHistoryMapper;

  /**
   * 워크플로우 런 조회
   *
   * @param pageParams pageParams
   * @return PageResult
   */
  @Override
  @Transactional(readOnly = true)
  public PageResult<WorkflowHistoryDTO> getPagedResult(PageParams pageParams) {

    return PageResult.from(
        pageParams,
        () -> workflowHistoryMapper.selectWorkflowHistoryList(pageParams),
        () -> workflowHistoryMapper.selectWorkflowHistoryCount(pageParams));
  }

  /**
   * 워크플로우 실행 상세 조회
   *
   * @param runId workflow_run.id
   * @return WorkflowRunDetailResponse
   */
  @Transactional(readOnly = true)
  public WorkflowRunDetailResponse getWorkflowRunDetail(Long runId) {
    // 1. 워크플로우 실행 정보 조회
    WorkflowRunDto workflowRunDto = workflowHistoryMapper.selectWorkflowRun(runId);

    // 2. Job 실행 목록 조회
    List<JobRunDto> jobRunDtos = workflowHistoryMapper.selectJobRunsByWorkflowRunId(runId);

    // 3. 각 Job의 Task 실행 목록 조회
    if (jobRunDtos != null) {
      jobRunDtos.forEach(
          jobRun -> {
            List<TaskRunDto> taskRuns =
                workflowHistoryMapper.selectTaskRunsByJobRunId(jobRun.getId());
            jobRun.setTaskRuns(taskRuns);
          });
    }

    // 4. TraceId 조회
    String traceId = workflowHistoryMapper.selectTraceIdByRunId(runId);

    return WorkflowRunDetailResponse.builder()
        .workflowRun(workflowRunDto)
        .jobRuns(jobRunDtos)
        .traceId(traceId)
        .build();
  }

  /**
   * 워크플로우 실행 로그 조회
   *
   * @param runId workflow_run.id
   * @return WorkflowRunLogsResponse
   */
  public WorkflowRunLogsResponse getWorkflowRunLogs(Long runId) {
    // TODO: 구현 예정
    return null;
  }

  /**
   * TraceId로 워크플로우 실행 조회
   *
   * @param traceId workflow_run.trace_id
   * @return WorkflowRunDetailResponse
   */
  public WorkflowRunDetailResponse getWorkflowRunByTraceId(String traceId) {
    // TODO: 구현 예정
    return null;
  }
}
