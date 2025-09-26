package site.icebang.domain.workflow.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.ApiResponse;
import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.domain.log.service.ExecutionLogService;
import site.icebang.domain.workflow.dto.WorkflowHistoryDTO;
import site.icebang.domain.workflow.dto.WorkflowRunDetailResponse;
import site.icebang.domain.workflow.dto.log.ExecutionLogSimpleDto;
import site.icebang.domain.workflow.dto.log.WorkflowLogQueryCriteria;
import site.icebang.domain.workflow.service.WorkflowHistoryService;

@RestController
@RequestMapping("/v0/workflow-runs")
@RequiredArgsConstructor
public class WorkflowHistoryController {
  private final WorkflowHistoryService workflowHistoryService;
  private final ExecutionLogService executionLogService;

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

  @GetMapping("/logs")
  public ApiResponse<List<ExecutionLogSimpleDto>> getTaskExecutionLog(
      @Valid @ModelAttribute WorkflowLogQueryCriteria requestDto) {
    return ApiResponse.success(
        ExecutionLogSimpleDto.from(executionLogService.getRawLogs(requestDto)));
  }
}
