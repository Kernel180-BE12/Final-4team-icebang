package site.icebang.domain.workflow.mapper;

import java.math.BigInteger;
import java.util.*;

import site.icebang.common.dto.PageParams;
import site.icebang.domain.workflow.dto.WorkflowCardDto;

public interface WorkflowMapper {
  List<WorkflowCardDto> selectWorkflowList(PageParams pageParams);

  int selectWorkflowCount(PageParams pageParams);

  WorkflowCardDto selectWorkflowById(BigInteger id);
}
