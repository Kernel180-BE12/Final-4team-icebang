package com.gltkorea.icebang.mapper;

import com.gltkorea.icebang.domain.schedule.model.Schedule; // import 경로 변경
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper {
    List<Schedule> findAllByIsActive(boolean isActive);
}