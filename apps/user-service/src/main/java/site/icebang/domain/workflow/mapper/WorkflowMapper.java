package site.icebang.domain.workflow.mapper;

import java.math.BigInteger;
import java.util.*;

import site.icebang.common.dto.PageParams;
import site.icebang.domain.workflow.dto.ScheduleDto;
import site.icebang.domain.workflow.dto.WorkflowCardDto;
import site.icebang.domain.workflow.dto.WorkflowDetailCardDto;

public interface WorkflowMapper {
  List<WorkflowCardDto> selectWorkflowList(PageParams pageParams);

  int selectWorkflowCount(PageParams pageParams);

  WorkflowCardDto selectWorkflowById(BigInteger id);

  WorkflowDetailCardDto selectWorkflowDetailById(BigInteger workflowId);

  List<ScheduleDto> selectSchedulesByWorkflowId(BigInteger workflowId);

  List<Map<String, Object>> selectWorkflowWithJobsAndTasks(BigInteger workflowId);
}
