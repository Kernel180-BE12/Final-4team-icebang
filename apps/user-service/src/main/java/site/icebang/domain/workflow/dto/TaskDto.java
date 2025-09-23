package site.icebang.domain.workflow.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class TaskDto {
  private Long id;
  private String name;
  private String type;
  private Integer executionOrder;
  private JsonNode parameters;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
