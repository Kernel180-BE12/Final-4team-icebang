package site.icebang.domain.execution.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JobRun {

  private Long id;
  private Long workflowRunId;
  private Long jobId;
  private String status; // PENDING, RUNNING, SUCCESS, FAILED
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private LocalDateTime createdAt;

  private JobRun(Long workflowRunId, Long jobId) {
    this.workflowRunId = workflowRunId;
    this.jobId = jobId;
    this.status = "RUNNING";
    this.startedAt = LocalDateTime.now();
    this.createdAt = this.startedAt;
  }

  /** Job 실행 시작을 위한 정적 팩토리 메소드 */
  public static JobRun start(Long workflowRunId, Long jobId) {
    return new JobRun(workflowRunId, jobId);
  }

  /** Job 실행 완료 처리 */
  public void finish(String status) {
    this.status = status;
    this.finishedAt = LocalDateTime.now();
  }
}
