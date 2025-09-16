package site.icebang.domain.workflow.mapper.execution;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.domain.workflow.model.execution.TaskRun;

@Mapper
public interface TaskRunMapper {
  void insert(TaskRun taskRun);

  void update(TaskRun taskRun);
}
