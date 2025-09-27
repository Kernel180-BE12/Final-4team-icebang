package site.icebang.domain.workflow.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class JobDto {
  private Long id;
  private String name;
  private String description;
  private Boolean isEnabled;
  private Instant createdAt;
  private Long createdBy;
  private Instant updatedAt;
  private Long updatedBy;

  private Integer executionOrder;
}
