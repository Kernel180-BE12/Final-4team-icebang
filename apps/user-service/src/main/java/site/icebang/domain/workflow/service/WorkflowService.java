package site.icebang.domain.workflow.service;

import java.util.List;

import org.springframework.stereotype.Service;

import site.icebang.common.dto.PageParams;
import site.icebang.domain.workflow.dto.WorkflowCardDto;

@Service
public class WorkflowService {
  public List<WorkflowCardDto> getWorkflowList(PageParams pageParams) {
    throw new RuntimeException("Not implemented");
  }

  public Integer getWorkflowCount(PageParams pageParams) {
    throw new RuntimeException("Not implemented");
  }
}
