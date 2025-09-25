package site.icebang.domain.workflow.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

  /**
   * 워크플로우 생성
   */
  @Transactional
  public void createWorkflow(WorkflowCreateDto dto, BigInteger createdBy) {
    // 1. 기본 검증
    validateBasicInput(dto, createdBy);

    // 2. 비즈니스 검증
    validateBusinessRules(dto);

    // 3. 중복체크
    if (workflowMapper.existsByName(dto.getName())) {
      throw new IllegalArgumentException("이미 존재하는 워크플로우 이름입니다 : " + dto.getName());
    }

    try {
      // 4. JSON 설정 생성
      String defaultConfigJson = dto.genertateDefaultConfigJson();
      dto.setDefaultConfigJson(defaultConfigJson);

      // 5. Workflow 삽입
      Map<String, Object> workflowParams = new HashMap<>();
      workflowParams.put("dto", dto);
      workflowParams.put("createdBy", createdBy);

      int result = workflowMapper.insertWorkflow(workflowParams);
      if (result != 1) {
        throw new RuntimeException("워크플로우 생성에 실패했습니다");
      }

      BigInteger workflowId = dto.getId();
      log.info("✅ Workflow 생성 완료 - ID: {}, Name: {}", workflowId, dto.getName());

      // 6. ⭐ 템플릿 기반 Job 생성
      List<WorkflowJobTemplate> jobTemplates = templateProvider.getTemplateByPlatform(
              dto.getPostingPlatform()
      );

      // 7. ⭐ Job 데이터 준비 (Batch Insert)
      List<Map<String, Object>> jobs = new ArrayList<>();
      for (WorkflowJobTemplate template : jobTemplates) {
        Map<String, Object> job = new HashMap<>();
        job.put("name", template.getName());
        job.put("description", template.getDescription());
        jobs.add(job);
      }

      // 8. ⭐ Job Batch Insert
      Map<String, Object> jobParams = new HashMap<>();
      jobParams.put("jobs", jobs);
      jobParams.put("createdBy", createdBy);
      workflowMapper.insertJobs(jobParams);

      log.info("✅ Job {} 개 Batch Insert 완료", jobs.size());

      // 9. ⭐ 생성된 Job ID 조회 (안전한 방법)
      List<Long> createdJobIds = workflowMapper.selectLastInsertedJobIds(createdBy);

      if (createdJobIds.size() != jobTemplates.size()) {
        throw new RuntimeException(
                String.format("Job 생성 개수 불일치: 예상=%d, 실제=%d",
                        jobTemplates.size(), createdJobIds.size())
        );
      }

      log.info("✅ 생성된 Job IDs: {}", createdJobIds);

      // 10. ⭐ Workflow-Job 연결 데이터 준비
      List<Map<String, Object>> workflowJobs = new ArrayList<>();
      for (int i = 0; i < jobTemplates.size(); i++) {
        Map<String, Object> wj = new HashMap<>();
        wj.put("workflowId", workflowId);
        wj.put("jobId", createdJobIds.get(i));
        wj.put("executionOrder", jobTemplates.get(i).getExecutionOrder());
        workflowJobs.add(wj);
      }

      // 11. ⭐ Workflow-Job 연결
      Map<String, Object> wjParams = new HashMap<>();
      wjParams.put("workflowJobs", workflowJobs);
      workflowMapper.insertWorkflowJobs(wjParams);

      log.info("✅ Workflow-Job 연결 완료 - {} 개", workflowJobs.size());

      // 12. ⭐ Job-Task 연결 데이터 준비
      List<Map<String, Object>> jobTasks = new ArrayList<>();
      for (int i = 0; i < jobTemplates.size(); i++) {
        Long jobId = createdJobIds.get(i);
        WorkflowJobTemplate template = jobTemplates.get(i);

        List<Integer> taskIds = template.getTaskIds();
        for (int j = 0; j < taskIds.size(); j++) {
          Map<String, Object> jt = new HashMap<>();
          jt.put("jobId", jobId);
          jt.put("taskId", taskIds.get(j));
          jt.put("executionOrder", j + 1);  // 1부터 시작
          jobTasks.add(jt);
        }
      }

      // 13. ⭐ Job-Task 연결
      Map<String, Object> jtParams = new HashMap<>();
      jtParams.put("jobTasks", jobTasks);
      workflowMapper.insertJobTasks(jtParams);

      log.info("✅ Job-Task 연결 완료 - {} 개", jobTasks.size());

      log.info("🎉 워크플로우 전체 생성 완료: {} (ID: {}, Jobs: {}, Tasks: {}, 생성자: {})",
              dto.getName(), workflowId, createdJobIds.size(), jobTasks.size(), createdBy);

    } catch (Exception e) {
      log.error("❌ 워크플로우 생성 실패: {}", dto.getName(), e);
      throw new RuntimeException("워크플로우 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
    }
  }

  /**
   * 기본 입력값 검증
   */
  private void validateBasicInput(WorkflowCreateDto dto, BigInteger createdBy) {
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
