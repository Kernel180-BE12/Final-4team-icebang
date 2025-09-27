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
public class WorkflowRunDto {
  private Long id; // workflow_run.id (실행 ID)
  private Long workflowId; // workflow.id (설계 ID)
  private String workflowName;
  private String workflowDescription;
  private String runNumber;
  private String status;
  private String triggerType;
  private Instant startedAt;
  private Instant finishedAt;
  private Integer durationMs;
  private Long createdBy;
  private Instant createdAt;
}
