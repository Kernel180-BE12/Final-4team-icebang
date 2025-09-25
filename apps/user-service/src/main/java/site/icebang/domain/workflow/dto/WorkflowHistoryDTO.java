package site.icebang.domain.workflow.dto;

import java.math.BigInteger;
import java.time.Instant;

import lombok.Data;

@Data
public class WorkflowHistoryDTO {

  private BigInteger id;
  private BigInteger workflowId;
  private String traceId;
  private Instant startedAt;
  private Instant finishedAt;
  private BigInteger createdBy;
  private String triggerType;
  private String runNumber;
  private String status;
}
