package site.icebang.domain.workflow.service;

import java.math.BigInteger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.common.service.PageableService;
import site.icebang.domain.workflow.dto.WorkflowCardDto;
import site.icebang.domain.workflow.mapper.WorkflowMapper;

@Service
@RequiredArgsConstructor
public class WorkflowService implements PageableService<WorkflowCardDto> {

  private final WorkflowMapper workflowMapper;

  @Override
  @Transactional(readOnly = true)
  public PageResult<WorkflowCardDto> getPagedResult(PageParams pageParams) {
    return PageResult.from(
        pageParams,
        () -> workflowMapper.selectWorkflowList(pageParams),
        () -> workflowMapper.selectWorkflowCount(pageParams));
  }
}
