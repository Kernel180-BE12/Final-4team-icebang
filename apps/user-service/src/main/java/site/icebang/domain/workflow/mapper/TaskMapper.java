package site.icebang.domain.workflow.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.domain.workflow.model.Task;

@Mapper
public interface TaskMapper {
  Optional<Task> findById(Long id);
}
