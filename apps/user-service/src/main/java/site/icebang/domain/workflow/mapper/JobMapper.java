package site.icebang.domain.workflow.mapper;

import site.icebang.domain.workflow.model.Job;
import site.icebang.domain.workflow.model.Task;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface JobMapper {
    List<Job> findJobsByWorkflowId(Long workflowId);
    List<Task> findTasksByJobId(Long jobId);
}