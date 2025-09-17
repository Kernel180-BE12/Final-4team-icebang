package site.icebang.domain.workflow.model;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Workflow {

  private Long id;
  private String name;
  private String description;
  private boolean isEnabled;
  private LocalDateTime createdAt;
  private Long createdBy;
  private LocalDateTime updatedAt;
  private Long updatedBy;

  /** 워크플로우별 기본 설정값 (JSON) */
  private String defaultConfig;
}
