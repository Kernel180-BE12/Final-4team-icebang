package site.icebang.schedule.runner;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import site.icebang.schedule.mapper.ScheduleMapper;
import site.icebang.schedule.model.Schedule;
import site.icebang.schedule.service.DynamicSchedulerService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerInitializer implements ApplicationRunner {

  private final ScheduleMapper scheduleMapper;
  private final DynamicSchedulerService dynamicSchedulerService;

  @Override
  public void run(ApplicationArguments args) {
    log.info(">>>> Initializing schedules from database...");
    List<Schedule> activeSchedules = scheduleMapper.findAllByIsActive(true);
    activeSchedules.forEach(dynamicSchedulerService::register);
    log.info(">>>> {} active schedules have been registered.", activeSchedules.size());
  }
}
