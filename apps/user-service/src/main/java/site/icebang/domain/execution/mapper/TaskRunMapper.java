package site.icebang.domain.execution.mapper;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.domain.execution.model.TaskRun;

@Mapper
public interface TaskRunMapper {
  void insert(TaskRun taskRun);

  void update(TaskRun taskRun);
}
