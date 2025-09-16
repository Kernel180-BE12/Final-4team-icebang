package site.icebang.domain.workflow.dto;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class WorkflowCardDto {
    private BigInteger id;
    private String name;
    private String description;
    private boolean isEnabled;
    private String createdBy;
    private LocalDateTime createdAt;
}