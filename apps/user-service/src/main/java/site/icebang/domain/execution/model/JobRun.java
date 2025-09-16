package site.icebang.domain.execution.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JobRun {
    private Long id;
    private Long workflowRunId;
    private Long jobId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer executionOrder;
    private LocalDateTime createdAt;
}