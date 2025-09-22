package site.icebang.domain.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRunDto {
  private Long id; // task_run.id (Task 실행 ID)
  private Long jobRunId; // job_run.id (관계)
  private Long taskId; // task.id (Task 설계 ID)
  private String taskName;
  private String taskDescription;
  private String taskType;
  private String status;
  private Integer executionOrder;
  private String startedAt;
  private String finishedAt;
  private Integer durationMs;
}
