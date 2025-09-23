package site.icebang.domain.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLogDto {
  private Long id; // execution_log.id
  private String executionType; // workflow, job, task
  private Long sourceId; // 모든 데이터에 대한 ID
  private Long runId; // 실행 ID (workflow_run, job_run, task_run)
  private String logLevel; // info, success, warning, error
  private String status; // running, success, failed, etc
  private String logMessage;
  private String executedAt;
  private Integer durationMs;
}
