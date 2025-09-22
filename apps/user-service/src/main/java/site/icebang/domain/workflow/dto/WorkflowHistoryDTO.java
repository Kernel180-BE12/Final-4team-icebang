package site.icebang.domain.workflow.dto;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class WorkflowHistoryDTO {

    private BigInteger id;
    private BigInteger workflowId;
    private String traceId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private BigInteger createdBy;
    private String triggerType;
    private String runNumber;
    private String status;
}
