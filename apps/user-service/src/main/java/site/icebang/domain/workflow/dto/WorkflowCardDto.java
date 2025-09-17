package site.icebang.domain.workflow.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkflowCardDto {
  private Long id;
  private String name;
  private String description;
  private boolean isEnabled;
}
