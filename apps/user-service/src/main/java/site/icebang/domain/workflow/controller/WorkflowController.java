package site.icebang.domain.workflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.ApiResponse;
import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.domain.workflow.dto.WorkflowCardDto;
import site.icebang.domain.workflow.service.WorkflowExecutionService;
import site.icebang.domain.workflow.service.WorkflowService;

@RestController
@RequestMapping("/v0/workflows")
@RequiredArgsConstructor
public class WorkflowController {
  private final WorkflowService workflowService;
  private final WorkflowExecutionService workflowExecutionService;

  @GetMapping("")
  public ApiResponse<PageResult<WorkflowCardDto>> getWorkflowList(
      @ModelAttribute PageParams pageParams) {
    PageResult<WorkflowCardDto> result = workflowService.getPagedResult(pageParams);
    return ApiResponse.success(result);
  }

  @PostMapping("/{workflowId}/run")
  public ResponseEntity<Void> runWorkflow(@PathVariable Long workflowId) {
    // HTTP 요청/응답 스레드를 블로킹하지 않도록 비동기 실행
    workflowExecutionService.executeWorkflow(workflowId);
    return ResponseEntity.accepted().build();
  }
}
