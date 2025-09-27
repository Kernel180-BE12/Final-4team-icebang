package site.icebang.domain.workflow.dto;

import java.time.Instant;

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
  private Instant startedAt;
  private Instant finishedAt;
  private Integer durationMs;
}
