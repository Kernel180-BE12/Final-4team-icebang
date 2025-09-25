package site.icebang.domain.workflow.model;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import site.icebang.domain.workflow.dto.JobDto;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Job {
  private Long id;
  private String name;
  private String description;
  private boolean isEnabled;
  private LocalDateTime createdAt;
  private Long createdBy;
  private LocalDateTime updatedAt;
  private Long updatedBy;

  public Job(JobDto dto) {
    this.id = dto.getId();
    this.name = dto.getName();
    this.description = dto.getDescription();
    this.isEnabled = dto.getIsEnabled();
    this.createdAt = dto.getCreatedAt();
    this.updatedAt = dto.getUpdatedAt();
  }
}
