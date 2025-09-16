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


  /**
   * 지정된 ID의 워크플로우를 수동으로 실행합니다.
   *
   * @param workflowId 실행할 워크플로우의 ID
   * @return HTTP 202 Accepted
   */
  @PostMapping("/{workflowId}/execute")
  public ResponseEntity<Void> executeWorkflow(@PathVariable Long workflowId) {
    // TODO: Spring Security 등 인증 체계에서 실제 사용자 ID를 가져와야 합니다.
    Long currentUserId = 1L; // 임시 사용자 ID

    // 워크플로우 실행 서비스 호출. 'MANUAL' 타입으로 실행을 요청합니다.
    // @Async로 동작하므로, 이 호출은 즉시 반환되고 워크플로우는 백그라운드에서 실행됩니다.
    workflowExecutionService.execute(workflowId, "MANUAL", currentUserId);

    // 작업이 성공적으로 접수되었음을 알리는 202 Accepted 상태를 반환합니다.
    return ResponseEntity.accepted().build();
  }
}
