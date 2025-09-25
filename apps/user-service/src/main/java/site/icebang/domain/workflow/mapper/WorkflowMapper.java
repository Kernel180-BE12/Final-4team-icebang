package site.icebang.domain.workflow.mapper;

import java.math.BigInteger;
import java.util.*;

import site.icebang.common.dto.PageParams;
import site.icebang.domain.workflow.dto.ScheduleDto;
import site.icebang.domain.workflow.dto.WorkflowCardDto;
import site.icebang.domain.workflow.dto.WorkflowCreateDto;
import site.icebang.domain.workflow.dto.WorkflowDetailCardDto;

public interface WorkflowMapper {
  List<WorkflowCardDto> selectWorkflowList(PageParams pageParams);

  int selectWorkflowCount(PageParams pageParams);

  int insertWorkflow(Map<String, Object> params); // insert workflow

  // Job 생성 관련 메서드
  void insertDefaultJobs(Map<String, Object> params);
  void insertJobTask(Map<String, Object> params);
  void insertWorkflowJob(Map<String, Object> params);

  boolean existsByName(String name);

  WorkflowCardDto selectWorkflowById(BigInteger id);

  WorkflowDetailCardDto selectWorkflowDetailById(BigInteger workflowId);

  List<ScheduleDto> selectSchedulesByWorkflowId(BigInteger workflowId);

  List<Map<String, Object>> selectWorkflowWithJobsAndTasks(BigInteger workflowId);
}
