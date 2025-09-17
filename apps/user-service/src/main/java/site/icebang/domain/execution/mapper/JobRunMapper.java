package site.icebang.domain.execution.mapper;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.domain.execution.model.JobRun;

@Mapper
public interface JobRunMapper {
  void insert(JobRun jobRun);

  void update(JobRun jobRun);
}
