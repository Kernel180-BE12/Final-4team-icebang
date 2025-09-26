package site.icebang.domain.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

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

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private String startedAt;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private String finishedAt;

  private Integer durationMs;
}
