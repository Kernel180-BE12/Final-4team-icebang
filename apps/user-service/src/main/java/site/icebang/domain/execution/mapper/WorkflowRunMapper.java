package site.icebang.domain.execution.mapper;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.domain.execution.model.WorkflowRun;

@Mapper
public interface WorkflowRunMapper {
  void insert(WorkflowRun workflowRun);

  void update(WorkflowRun workflowRun);
}
