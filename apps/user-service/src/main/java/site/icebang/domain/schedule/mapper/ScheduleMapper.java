package site.icebang.domain.schedule.mapper;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import site.icebang.domain.schedule.model.Schedule;

@Mapper
public interface ScheduleMapper {
  void save(Schedule schedule);
  void update(Schedule schedule);
  Optional<Schedule> findById(Long id);
  List<Schedule> findAllActive();
}