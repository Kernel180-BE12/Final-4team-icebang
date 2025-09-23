package site.icebang.domain.workflow.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobDto {
    private Long id;
    private String name;
    private String description;
    private Boolean isEnabled;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    private Integer executionOrder;
}
