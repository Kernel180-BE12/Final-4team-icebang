package site.icebang.domain.schedule.service;

import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.icebang.domain.schedule.model.Schedule;
import site.icebang.domain.schedule.mapper.ScheduleMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final DynamicSchedulerService dynamicSchedulerService;

    /**
     * 애플리케이션 시작 시 활성화된 모든 스케줄을 초기화합니다.
     * 이를 통해 서버가 재시작되어도 스케줄이 자동으로 복원됩니다.
     */
    @PostConstruct
    public void initializeSchedules() {
        log.info("Initializing active schedules from database...");
        List<Schedule> activeSchedules = scheduleMapper.findAllActive();
        activeSchedules.forEach(dynamicSchedulerService::register);
        log.info("{} active schedules have been initialized.", activeSchedules.size());
    }

    @Transactional
    public Schedule createSchedule(Schedule schedule) {
        // 1. DB에 스케줄 저장
        scheduleMapper.save(schedule);

        // 2. 메모리의 스케줄러에 동적으로 등록
        if (schedule.isActive()) { // Lombok Getter for boolean is isActive()
            dynamicSchedulerService.register(schedule);
        }

        return schedule;
    }

    // TODO: 스케줄 수정 로직(updateSchedule) 구현이 필요합니다.

    @Transactional
    public void deactivateSchedule(Long scheduleId) {
        // 1. DB에서 스케줄을 비활성화
        Schedule schedule = scheduleMapper.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + scheduleId));
        schedule.setActive(false);
        scheduleMapper.update(schedule);

        // 2. 메모리의 스케줄러에서 제거
        dynamicSchedulerService.remove(scheduleId);
    }
}