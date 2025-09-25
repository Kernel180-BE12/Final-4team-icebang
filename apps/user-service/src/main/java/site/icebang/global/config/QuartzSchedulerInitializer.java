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

/**
 * 애플리케이션 시작 시 데이터베이스에 저장된 스케줄을 Quartz 스케줄러에 동적으로 등록하는 초기화 클래스입니다.
 *
 * <p>이 클래스는 {@code ApplicationListener<ContextRefreshedEvent>}를 구현하여, Spring의 ApplicationContext가
 * 완전히 초기화되고 모든 Bean이 준비되었을 때 단 한 번 실행됩니다. 데이터베이스의 {@code schedule} 테이블을
 * 'Source of Truth'로 삼아, 활성화된 모든 스케줄을 읽어와 Quartz 엔진에 동기화하는 매우 중요한 역할을 수행합니다.
 *
 * <h2>주요 기능:</h2>
 * <ul>
 * <li>애플리케이션 컨텍스트 초기화 완료 시점에 DB의 활성 스케줄 조회</li>
 * <li>조회된 스케줄을 {@code QuartzScheduleService}를 통해 Quartz 엔진에 등록</li>
 * </ul>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzSchedulerInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final ScheduleMapper scheduleMapper;
    private final QuartzScheduleService quartzScheduleService;

    /**
     * Spring ApplicationContext가 완전히 새로고침(초기화)될 때 호출되는 이벤트 핸들러 메소드입니다.
     *
     * <p>데이터베이스에서 활성화된 모든 스케줄을 조회하고, 각 스케줄을
     * {@code QuartzScheduleService}를 통해 Quartz 스케줄러에 등록합니다.
     *
     * @param event 발생한 ContextRefreshedEvent 객체
     * @since v0.1.0
     */
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