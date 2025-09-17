package site.icebang.domain.schedule.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.domain.schedule.model.Schedule;

@Mapper
public interface ScheduleMapper {
  List<Schedule> findAllActive();
}
