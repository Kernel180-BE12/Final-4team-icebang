package site.icebang.domain.workflow.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class WorkflowDetailCardDto extends WorkflowCardDto {
  private String defaultConfig;
  private LocalDateTime updatedAt;
  private String updatedBy;
  private List<ScheduleDto> schedules;
  private List<Map<String, Object>> jobs;
}
