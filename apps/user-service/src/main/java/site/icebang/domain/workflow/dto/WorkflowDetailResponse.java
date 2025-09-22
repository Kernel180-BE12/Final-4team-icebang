package site.icebang.domain.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRunDetailResponse {
  private String traceId;
  private WorkflowRunDto workflowRun;
  private List<JobRunDto> jobRuns;
}
