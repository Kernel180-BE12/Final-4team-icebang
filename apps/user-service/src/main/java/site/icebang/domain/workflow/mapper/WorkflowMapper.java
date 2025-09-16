package site.icebang.domain.workflow.mapper;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import site.icebang.domain.workflow.model.Workflow;

@Mapper
public interface WorkflowMapper {
    Optional<Workflow> findById(Long id);
}