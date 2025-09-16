package site.icebang.domain.workflow.model.execution;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkflowRun {

  private Long id;
  private Long workflowId;
  private String traceId; // 분산 추적을 위한 ID
  private String status; // PENDING, RUNNING, SUCCESS, FAILED
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private LocalDateTime createdAt;

  private WorkflowRun(Long workflowId) {
    this.workflowId = workflowId;
    this.traceId = UUID.randomUUID().toString(); // 고유 추적 ID 생성
    this.status = "RUNNING";
    this.startedAt = LocalDateTime.now();
    this.createdAt = this.startedAt;
  }

  /** 워크플로우 실행 시작을 위한 정적 팩토리 메소드 */
  public static WorkflowRun start(Long workflowId) {
    return new WorkflowRun(workflowId);
  }

  /** 워크플로우 실행 완료 처리 */
  public void finish(String status) {
    this.status = status;
    this.finishedAt = LocalDateTime.now();
  }
}
