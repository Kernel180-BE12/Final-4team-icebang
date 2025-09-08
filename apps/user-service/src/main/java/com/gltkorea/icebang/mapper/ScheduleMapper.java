package com.gltkorea.icebang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.gltkorea.icebang.domain.schedule.model.Schedule;

@Mapper
public interface ScheduleMapper {
  List<Schedule> findAllByIsActive(boolean isActive);
}
