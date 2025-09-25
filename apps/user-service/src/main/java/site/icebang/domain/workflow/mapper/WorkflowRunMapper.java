package site.icebang.domain.workflow.mapper;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.domain.workflow.model.WorkflowRun;

@Mapper
public interface WorkflowRunMapper {
  void insert(WorkflowRun workflowRun);

  void update(WorkflowRun workflowRun);
}
