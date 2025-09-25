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

/**
 * 워크플로우의 '정의'와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 *
 * <p>이 서비스는 워크플로우의 실행(Execution)이 아닌, 생성된 워크플로우의 구조를 조회하는 기능에 집중합니다.
 *
 * <h2>주요 기능:</h2>
 *
 * <ul>
 *   <li>워크플로우 목록 페이징 조회
 *   <li>특정 워크플로우의 상세 구조 조회 (Job, Task, Schedule 포함)
 * </ul>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
@Service
@RequiredArgsConstructor
public class WorkflowService implements PageableService<WorkflowCardDto> {

  private final WorkflowMapper workflowMapper;

  /**
   * 워크플로우 목록을 페이징 처리하여 조회합니다.
   *
   * <p>이 메소드는 {@code PageableService} 인터페이스를 구현하며, {@code PageResult} 유틸리티를 사용하여 전체 카운트 쿼리와 목록 조회
   * 쿼리를 실행하고 페이징 결과를 생성합니다.
   *
   * @param pageParams 페이징 처리에 필요한 파라미터 (페이지 번호, 페이지 크기 등)
   * @return 페이징 처리된 워크플로우 카드 목록
   * @see PageResult
   * @since v0.1.0
   */
  @Override
  @Transactional(readOnly = true)
  public PageResult<WorkflowCardDto> getPagedResult(PageParams pageParams) {
    return PageResult.from(
        pageParams,
        () -> workflowMapper.selectWorkflowList(pageParams),
        () -> workflowMapper.selectWorkflowCount(pageParams));
  }

  /**
   * 특정 워크플로우의 상세 구조를 조회합니다.
   *
   * <p>지정된 워크플로우 ID에 해당하는 기본 정보, 연결된 스케줄 목록, 그리고 Job과 Task의 계층 구조를 모두 조회하여 하나의 DTO로 조합하여 반환합니다.
   *
   * @param workflowId 조회할 워크플로우의 ID
   * @return 워크플로우의 전체 구조를 담은 상세 DTO
   * @throws IllegalArgumentException 주어진 ID에 해당하는 워크플로우가 존재하지 않을 경우
   * @since v0.1.0
   */
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
