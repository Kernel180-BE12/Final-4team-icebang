package site.icebang.domain.mapping.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowJobMapper {
    // A workflow can have multiple jobs, ordered by execution_order
    List<Long> findJobIdsByWorkflowId(Long workflowId);
}