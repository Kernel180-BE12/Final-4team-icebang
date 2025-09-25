package site.icebang.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import site.icebang.domain.schedule.model.Schedule;
import site.icebang.domain.schedule.mapper.ScheduleMapper;
import site.icebang.domain.schedule.service.QuartzScheduleService;
import java.util.List;

/**
 * 애플리케이션 시작 시 데이터베이스에 저장된 스케줄을 Quartz 스케줄러에 동적으로 등록하는 초기화 클래스입니다.
 *
 * <p>이 클래스는 {@code CommandLineRunner}를 구현하여, Spring Boot 애플리케이션이 완전히
 * 로드된 후 단 한 번 실행됩니다. 데이터베이스의 {@code schedule} 테이블을 'Source of Truth'로 삼아,
 * 활성화된 모든 스케줄을 읽어와 Quartz 엔진에 동기화하는 매우 중요한 역할을 수행합니다.
 *
 * <h2>주요 기능:</h2>
 * <ul>
 * <li>애플리케이션 시작 시점에 DB의 활성 스케줄 조회</li>
 * <li>조회된 스케줄을 {@code QuartzScheduleService}를 통해 Quartz 엔진에 등록</li>
 * </ul>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzSchedulerInitializer implements CommandLineRunner {

    private final ScheduleMapper scheduleMapper;
    private final QuartzScheduleService quartzScheduleService;

    /**
     * Spring Boot 애플리케이션 시작 시 호출되는 메인 실행 메소드입니다.
     *
     * <p>데이터베이스에서 활성화된 모든 스케줄을 조회하고, 각 스케줄을
     * {@code QuartzScheduleService}를 통해 Quartz 스케줄러에 등록합니다.
     *
     * @param args 애플리케이션 실행 시 전달되는 인자
     * @since v0.1.0
     */
    @Override
    public void run(String... args) {
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