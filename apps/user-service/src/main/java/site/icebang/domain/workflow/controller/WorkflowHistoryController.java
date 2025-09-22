package site.icebang.domain.workflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.ApiResponse;
import site.icebang.domain.workflow.dto.WorkflowRunDetailResponse;
import site.icebang.domain.workflow.service.WorkflowHistoryService;

@RestController
@RequestMapping("/v0/workflow-runs")
@RequiredArgsConstructor
public class WorkflowHistoryController {
  private final WorkflowHistoryService workflowHistoryService;

  /**
   * 워크플로우 실행 상세 조회
   *
   * @param runId workflow_run.id
   * @return WorkflowRunDetailResponse
   */
  @GetMapping("/{runId}")
  public ApiResponse<WorkflowRunDetailResponse> getWorkflowRunDetail(@PathVariable Long runId) {
    WorkflowRunDetailResponse response = workflowHistoryService.getWorkflowRunDetail(runId);
    return ApiResponse.success(response);
  }
}
