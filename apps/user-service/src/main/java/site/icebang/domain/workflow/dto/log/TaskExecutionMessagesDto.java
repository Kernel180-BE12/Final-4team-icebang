package site.icebang.domain.workflow.dto.log;

import java.util.List;
import java.util.stream.Collectors;

import site.icebang.domain.workflow.dto.ExecutionLogDto;

public record TaskExecutionMessagesDto(List<String> messages) {
  public static TaskExecutionMessagesDto from(List<ExecutionLogDto> executionLogList) {
    List<String> messages =
        executionLogList.stream().map(ExecutionLogDto::getLogMessage).collect(Collectors.toList());

    return new TaskExecutionMessagesDto(messages);
  }
}
