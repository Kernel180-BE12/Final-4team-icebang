package site.icebang.domain.schedule.model;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter // 서비스 레이어에서의 상태 변경 및 MyBatis 매핑을 위해 사용
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Schedule {

  private Long id;
  private Long workflowId;
  private String cronExpression;
  private String parameters; // JSON format
  private boolean isActive;
  private String lastRunStatus;
  private LocalDateTime lastRunAt;
  private LocalDateTime createdAt;
  private Long createdBy;
  private LocalDateTime updatedAt;
  private Long updatedBy;
  private String scheduleText;
}
