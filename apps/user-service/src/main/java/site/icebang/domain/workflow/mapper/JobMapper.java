package site.icebang.domain.workflow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.domain.workflow.dto.TaskDto;
import site.icebang.domain.workflow.model.Job;

@Mapper
public interface JobMapper {
  List<Job> findJobsByWorkflowId(Long workflowId);

  List<TaskDto> findTasksByJobId(Long jobId);
}
