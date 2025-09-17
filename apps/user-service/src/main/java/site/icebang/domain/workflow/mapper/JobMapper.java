package site.icebang.domain.workflow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.domain.workflow.model.Job;
import site.icebang.domain.workflow.model.Task;

@Mapper
public interface JobMapper {
  List<Job> findJobsByWorkflowId(Long workflowId);

  List<Task> findTasksByJobId(Long jobId);
}
