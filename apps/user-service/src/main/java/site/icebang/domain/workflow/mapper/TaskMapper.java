package site.icebang.domain.workflow.mapper;

import site.icebang.domain.workflow.model.Task;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

@Mapper
public interface TaskMapper {
    Optional<Task> findById(Long id);
}