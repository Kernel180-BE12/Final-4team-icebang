package site.icebang.domain.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckEmailResponse {
  private Boolean available;
}
