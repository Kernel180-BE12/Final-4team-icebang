package site.icebang.domain.workflow.mapper.execution;

import site.icebang.domain.workflow.model.execution.TaskRun;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskRunMapper {
    void insert(TaskRun taskRun);
    void update(TaskRun taskRun);
}