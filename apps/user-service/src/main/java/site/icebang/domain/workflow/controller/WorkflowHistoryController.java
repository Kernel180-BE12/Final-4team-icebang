package site.icebang.domain.workflow.controller;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.ApiResponse;
import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.domain.workflow.dto.WorkflowHistoryDTO;
import site.icebang.domain.workflow.dto.WorkflowRunDetailResponse;
import site.icebang.domain.workflow.service.WorkflowHistoryService;

@RestController
@RequestMapping("/v0/workflow-runs")
@RequiredArgsConstructor
public class WorkflowHistoryController {
  private final WorkflowHistoryService workflowHistoryService;

  @GetMapping("")
  public ApiResponse<PageResult<WorkflowHistoryDTO>> getWorkflowHistoryList(
      @ModelAttribute PageParams pageParams) {
    PageResult<WorkflowHistoryDTO> response = workflowHistoryService.getPagedResult(pageParams);
    return ApiResponse.success(response);
  }

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
