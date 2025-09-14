package site.icebang.domain.workflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.ApiResponse;
import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.common.service.PageService;
import site.icebang.domain.workflow.dto.WorkflowCardDto;
import site.icebang.domain.workflow.service.WorkflowService;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {
  private final PageService pageService;
  private final WorkflowService workflowService;

  @GetMapping("")
  public ApiResponse<PageResult<WorkflowCardDto>> getWorkflowList(
      @ModelAttribute PageParams pageParams) {
    return pageService.createPagedResponse(
        pageParams, workflowService::getWorkflowList, workflowService::getWorkflowCount);
  }
}
