package site.icebang.schedule.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Schedule {
  private Long scheduleId;
  private Long workflowId;
  private String cronExpression;
  private boolean isActive;
  // ... 기타 필요한 컬럼
}
