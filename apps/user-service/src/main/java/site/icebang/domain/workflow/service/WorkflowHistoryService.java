package site.icebang.domain.workflow.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.workflow.dto.WorkflowRunDetailResponse;
import site.icebang.domain.workflow.dto.WorkflowRunLogsResponse;

@Service
@RequiredArgsConstructor
public class WorkflowHistoryService {

  /**
   * 워크플로우 실행 상세 조회
   *
   * @param runId workflow_run.id
   * @return WorkflowRunDetailResponse
   */
  public WorkflowRunDetailResponse getWorkflowRunDetail(Long runId) {
    // TODO: 구현 예정
    return null;
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
