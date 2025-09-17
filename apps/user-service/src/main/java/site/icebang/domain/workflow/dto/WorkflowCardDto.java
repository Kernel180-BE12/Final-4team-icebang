package site.icebang.domain.workflow.dto;

import java.math.BigInteger;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class WorkflowCardDto {
  private BigInteger id;
  private String name;
  private String description;
  private boolean isEnabled;
  private String createdBy;
  private LocalDateTime createdAt;
}
