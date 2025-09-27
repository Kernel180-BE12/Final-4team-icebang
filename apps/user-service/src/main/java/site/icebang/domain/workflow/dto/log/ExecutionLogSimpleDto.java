package site.icebang.domain.workflow.dto.log;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import site.icebang.domain.workflow.dto.ExecutionLogDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLogSimpleDto {
  private String logLevel;
  private String logMessage;
  private Instant executedAt;

  public static ExecutionLogSimpleDto from(ExecutionLogDto executionLogDto) {
    return ExecutionLogSimpleDto.builder()
        .logLevel(executionLogDto.getLogLevel())
        .logMessage(executionLogDto.getLogMessage())
        .executedAt(executionLogDto.getExecutedAt())
        .build();
  }

  public static List<ExecutionLogSimpleDto> from(List<ExecutionLogDto> executionLogList) {
    return executionLogList.stream().map(ExecutionLogSimpleDto::from).collect(Collectors.toList());
  }
}
