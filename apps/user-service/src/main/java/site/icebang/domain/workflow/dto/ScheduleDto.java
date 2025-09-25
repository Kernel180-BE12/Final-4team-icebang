package site.icebang.domain.workflow.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ScheduleDto {
  private Long id;
  private String cronExpression;
  private Boolean isActive;
  private String lastRunStatus;
  private LocalDateTime lastRunAt;
  private String scheduleText;
  private LocalDateTime createdAt;
}
