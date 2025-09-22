package site.icebang.domain.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRunDto {
  private Long id; // job_run.id (Job 실행 ID)
  private Long workflowRunId; // workflow_run.id (관계)
  private Long jobId; // job.id (Job 설계 ID)
  private String jobName;
  private String jobDescription;
  private String status;
  private Integer executionOrder;
  private String startedAt;
  private String finishedAt;
  private Integer durationMs;
  private List<TaskRunDto> taskRuns;
}
