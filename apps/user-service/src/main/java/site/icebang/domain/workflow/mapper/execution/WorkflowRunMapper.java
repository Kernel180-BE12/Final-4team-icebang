package site.icebang.domain.workflow.mapper.execution;

import org.apache.ibatis.annotations.Mapper;
import site.icebang.domain.workflow.model.execution.WorkflowRun;

@Mapper
public interface WorkflowRunMapper {
    void insert(WorkflowRun workflowRun);
    void update(WorkflowRun workflowRun);
}