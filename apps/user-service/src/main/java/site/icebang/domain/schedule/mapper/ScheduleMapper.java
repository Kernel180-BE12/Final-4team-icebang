package site.icebang.domain.schedule.mapper;

import org.apache.ibatis.annotations.Mapper;
import site.icebang.domain.schedule.model.Schedule;
import java.util.List;

@Mapper
public interface ScheduleMapper {
  List<Schedule> findAllActive();
}