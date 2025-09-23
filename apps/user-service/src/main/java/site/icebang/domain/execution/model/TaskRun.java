package site.icebang.domain.execution.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TaskRun {

  private Long id;
  private Long jobRunId;
  private Long taskId;
  private Integer executionOrder;
  private String status; // PENDING, RUNNING, SUCCESS, FAILED
  private String resultMessage; // 실행 결과 메시지
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private LocalDateTime createdAt;

  // 생성자나 정적 팩토리 메서드를 통해 객체 생성 로직을 관리
  private TaskRun(Long jobRunId, Long taskId) {
    this.jobRunId = jobRunId;
    this.taskId = taskId;
    this.status = "PENDING";
    this.createdAt = LocalDateTime.now();
  }

  /** Task 실행 시작을 위한 정적 팩토리 메서드 */
  public static TaskRun start(Long jobRunId, Long taskId,  Integer executionOrder) {
    TaskRun taskRun = new TaskRun(jobRunId, taskId);
    taskRun.executionOrder = executionOrder;
    taskRun.status = "RUNNING";
    taskRun.startedAt = LocalDateTime.now();
    return taskRun;
  }

  /** Task 실행 완료 처리 */
  public void finish(String status, String resultMessage) {
    this.status = status;
    this.resultMessage = resultMessage;
    this.finishedAt = LocalDateTime.now();
  }
}
