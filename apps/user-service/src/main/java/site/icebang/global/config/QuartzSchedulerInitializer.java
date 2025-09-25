package site.icebang.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import site.icebang.domain.schedule.model.Schedule;
import site.icebang.domain.schedule.mapper.ScheduleMapper;
import site.icebang.domain.schedule.service.QuartzScheduleService;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzSchedulerInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final ScheduleMapper scheduleMapper;
    private final QuartzScheduleService quartzScheduleService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Quartz 스케줄러 초기화 시작: DB 스케줄을 등록합니다.");
        try {
            List<Schedule> activeSchedules = scheduleMapper.findAllActive();
            for (Schedule schedule : activeSchedules) {
                quartzScheduleService.addOrUpdateSchedule(schedule);
            }
            log.info("총 {}개의 활성 스케줄을 Quartz에 성공적으로 등록했습니다.", activeSchedules.size());
        } catch (Exception e) {
            log.error("Quartz 스케줄 초기화 중 오류가 발생했습니다.", e);
        }
    }
}