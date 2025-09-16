package site.icebang.schedule.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.schedule.model.Schedule;

@Mapper
public interface ScheduleMapper {
  List<Schedule> findAllByIsActive(boolean isActive);
}
