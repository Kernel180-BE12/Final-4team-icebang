package site.icebang.domain.log.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.log.mapper.ExecutionLogMapper;
import site.icebang.domain.workflow.dto.ExecutionLogDto;
import site.icebang.domain.workflow.dto.log.WorkflowLogQueryCriteria;

@Service
@RequiredArgsConstructor
public class ExecutionLogService {
  private final ExecutionLogMapper executionLogMapper;

  public List<ExecutionLogDto> getRawLogs(WorkflowLogQueryCriteria criteria) {
    return executionLogMapper.selectLogsByCriteria(criteria);
  }
}
