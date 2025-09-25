package site.icebang.domain.workflow.service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.common.service.PageableService;
import site.icebang.domain.workflow.dto.ScheduleDto;
import site.icebang.domain.workflow.dto.WorkflowCardDto;
import site.icebang.domain.workflow.dto.WorkflowCreateDto;
import site.icebang.domain.workflow.dto.WorkflowDetailCardDto;
import site.icebang.domain.workflow.mapper.WorkflowMapper;

@Slf4j
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

  /**
   * 워크플로우 생성
   */
  @Transactional
  public void createWorkflow(WorkflowCreateDto dto, Long createdBy) {
    // 1. 기본 검증
    validateBasicInput(dto, createdBy);

    // 2. 비즈니스 검증
    validateBusinessRules(dto);

    // 3. 중복체크
    if (workflowMapper.existsByName(dto.getName())) {
      throw new IllegalArgumentException("이미 존재하는 워크플로우 이름입니다 : " + dto.getName());
    }

    // 4. 워크플로우 생성
    try {
      // JSON 설정 생성
      String defaultConfigJson = dto.genertateDefaultConfigJson();
      dto.setDefaultConfigJson(defaultConfigJson);

      //DB 삽입 파라미터 구성
      Map<String, Object> params = new HashMap<>();
      params.put("dto", dto);
      params.put("createdBy", createdBy);

      int result = workflowMapper.insertWorkflow(params);
      if (result != 1) {
        throw new RuntimeException("워크플로우 생성에 실패했습니다");
      }

      log.info("워크플로우 생성 완료: {} (생성자: {})", dto.getName(), createdBy);

    } catch (Exception e) {
      log.error("워크플로우 생성 실패: {}", dto.getName(), e);
      throw new RuntimeException("워크플로우 생성 중 오류가 발생했습니다", e);
    }
  }

  /**
   * 기본 입력값 검증
   */
  private void validateBasicInput(WorkflowCreateDto dto, Long createdBy) {
    if (dto == null) {
      throw new IllegalArgumentException("워크플로우 정보가 필요합니다");
    }
    if (createdBy == null) {
      throw new IllegalArgumentException("생성자 정보가 필요합니다");
    }
  }

  /**
   * 비즈니스 규칙 검증
   */
  private void validateBusinessRules(WorkflowCreateDto dto) {
    // 포스팅 플랫폼 선택 시 계정 정보 필수 검증
    String postingPlatform = dto.getPostingPlatform();
    if (postingPlatform != null && !postingPlatform.isBlank()) {
      if (dto.getPostingAccountId() == null || dto.getPostingAccountId().isBlank()) {
        throw new IllegalArgumentException("포스팅 플랫폼 선택 시 계정 ID는 필수입니다");
      }
      if (dto.getPostingAccountPassword() == null || dto.getPostingAccountPassword().isBlank()) {
        throw new IllegalArgumentException("포스팅 플랫폼 선택 시 계정 비밀번호는 필수입니다");
      }
      // 티스토리 블로그 추가 검증
      if ("tstory_blog".equals(postingPlatform)) {
        if (dto.getBlogName() == null || dto.getBlogName().isBlank()) {
          throw new IllegalArgumentException("티스토리 블로그 선택 시 블로그 이름은 필수입니다");
        }
      }
    }
  }
}
