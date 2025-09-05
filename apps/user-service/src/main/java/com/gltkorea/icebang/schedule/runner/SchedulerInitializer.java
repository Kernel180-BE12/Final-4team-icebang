package com.gltkorea.icebang.schedule.runner;

import com.gltkorea.icebang.domain.schedule.model.Schedule;
import com.gltkorea.icebang.mapper.ScheduleMapper;
import com.gltkorea.icebang.schedule.service.DynamicSchedulerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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