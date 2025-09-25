package site.icebang.domain.workflow.controller;

import java.math.BigInteger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.ApiResponse;
import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.domain.auth.model.AuthCredential;
import site.icebang.domain.workflow.dto.WorkflowCardDto;
import site.icebang.domain.workflow.dto.WorkflowCreateDto;
import site.icebang.domain.workflow.dto.WorkflowDetailCardDto;
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

  @PostMapping("")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> createWorkflow(
      @Valid @RequestBody WorkflowCreateDto workflowCreateDto,
      @AuthenticationPrincipal AuthCredential authCredential) {
    // 인증 체크
    if (authCredential == null) {
      throw new IllegalArgumentException("로그인이 필요합니다");
    }

    // AuthCredential에서 userId 추출
    BigInteger userId = authCredential.getId();

    workflowService.createWorkflow(workflowCreateDto, userId);
    return ApiResponse.success(null);
  }

  @PostMapping("/{workflowId}/run")
  public ResponseEntity<Void> runWorkflow(@PathVariable Long workflowId) {
    // HTTP 요청/응답 스레드를 블로킹하지 않도록 비동기 실행
    workflowExecutionService.executeWorkflow(workflowId);
    return ResponseEntity.accepted().build();
  }

  @GetMapping("/{workflowId}/detail")
  public ApiResponse<WorkflowDetailCardDto> getWorkflowDetail(@PathVariable BigInteger workflowId) {
    WorkflowDetailCardDto result = workflowService.getWorkflowDetail(workflowId);
    return ApiResponse.success(result);
  }
}
