package site.icebang.domain.workflow.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.common.service.PageableService;
import site.icebang.domain.workflow.dto.ScheduleDto;
import site.icebang.domain.workflow.dto.WorkflowCardDto;
import site.icebang.domain.workflow.dto.WorkflowDetailCardDto;
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

  @Transactional(readOnly = true)
  public WorkflowDetailCardDto getWorkflowDetail(BigInteger workflowId) {

    // 1. 워크플로우 기본 정보 조회 (단일 row, 효율적)
    WorkflowDetailCardDto workflow = workflowMapper.selectWorkflowDetailById(workflowId);
    if (workflow == null) {
      throw new IllegalArgumentException("워크플로우를 찾을 수 없습니다: " + workflowId);
    }

    // 2. 스케줄 목록 조회 (별도 쿼리로 성능 최적화)
    List<ScheduleDto> schedules = workflowMapper.selectSchedulesByWorkflowId(workflowId);
    workflow.setSchedules(schedules);

    List<Map<String, Object>> jobs = workflowMapper.selectWorkflowWithJobsAndTasks(workflowId);
    workflow.setJobs(jobs);

    return workflow;
  }
}
