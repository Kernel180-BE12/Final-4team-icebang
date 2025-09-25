package site.icebang.domain.workflow.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class ScheduleDto {
  private Long id;
  private String cronExpression;
  private Boolean isActive;
  private String lastRunStatus;
  private Instant lastRunAt;
  private String scheduleText;
  private Instant createdAt;
}
