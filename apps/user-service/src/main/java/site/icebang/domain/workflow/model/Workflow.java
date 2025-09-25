package site.icebang.domain.workflow.model;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Workflow {

  private Long id;
  private String name;
  private String description;
  private boolean isEnabled;
  private Instant createdAt;
  private Long createdBy;
  private Instant updatedAt;
  private Long updatedBy;

  /** 워크플로우별 기본 설정값 (JSON) */
  private String defaultConfig;
}
