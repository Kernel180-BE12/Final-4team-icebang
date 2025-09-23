package site.icebang.domain.workflow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.common.dto.PageParams;
import site.icebang.domain.workflow.dto.JobRunDto;
import site.icebang.domain.workflow.dto.TaskRunDto;
import site.icebang.domain.workflow.dto.WorkflowHistoryDTO;
import site.icebang.domain.workflow.dto.WorkflowRunDto;

@Mapper
public interface WorkflowHistoryMapper {
  /**
   * 워크플로우 실행 정보 조회
   *
   * @param runId workflow_run.id
   * @return WorkflowRunDto
   */
  WorkflowRunDto selectWorkflowRun(Long runId);

  /**
   * 워크플로우 실행의 Job 목록 조회
   *
   * @param workflowRunId workflow_run.id
   * @return List<JobRunDto>
   */
  List<JobRunDto> selectJobRunsByWorkflowRunId(Long workflowRunId);

  /**
   * Job 실행의 Task 목록 조회
   *
   * @param jobRunId job_run.id
   * @return List<TaskRunDto>
   */
  List<TaskRunDto> selectTaskRunsByJobRunId(Long jobRunId);

  /**
   * 워크플로우 실행 TraceId 조회
   *
   * @param runId workflow_run.id
   * @return String traceId
   */
  String selectTraceIdByRunId(Long runId);

  /**
   * 워크플로우 런 페이지네이션
   *
   * @param pageParams pageParams
   * @return List<WorkflowHistoryDTO>
   */
  List<WorkflowHistoryDTO> selectWorkflowHistoryList(PageParams pageParams);

  /**
   * 워크플로우 런 인스턴스 개수 조회
   *
   * @param pageParams pageParams
   * @return 결과 개수
   */
  int selectWorkflowHistoryCount(PageParams pageParams);
}
