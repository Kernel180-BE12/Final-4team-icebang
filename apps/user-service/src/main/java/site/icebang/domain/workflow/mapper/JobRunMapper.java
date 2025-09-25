package site.icebang.domain.workflow.mapper;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.domain.workflow.model.JobRun;

@Mapper
public interface JobRunMapper {
  void insert(JobRun jobRun);

  void update(JobRun jobRun);
}
