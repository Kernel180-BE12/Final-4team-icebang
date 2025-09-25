package site.icebang.domain.schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import site.icebang.domain.schedule.model.Schedule;
import site.icebang.domain.workflow.scheduler.WorkflowTriggerJob;

/**
 * Spring Quartz 스케줄러의 Job과 Trigger를 동적으로 관리하는 서비스 클래스입니다.
 *
 * <p>이 서비스는 데이터베이스에 정의된 {@code Schedule} 정보를 바탕으로,
 * Quartz 엔진에 실제 실행 가능한 작업을 등록, 수정, 삭제하는 역할을 담당합니다.
 *
 * <h2>주요 기능:</h2>
 * <ul>
 * <li>DB의 스케줄 정보를 바탕으로 Quartz Job 및 Trigger 생성 또는 업데이트</li>
 * <li>기존에 등록된 Quartz 스케줄 삭제</li>
 * </ul>
 *
 * @author jihu0210@naver.com
 * @since v0.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzScheduleService {

    /** Quartz 스케줄러의 메인 인스턴스 */
    private final Scheduler scheduler;

    /**
     * DB에 정의된 Schedule 객체를 기반으로 Quartz에 스케줄을 등록하거나 업데이트합니다.
     *
     * <p>지정된 워크플로우 ID에 해당하는 Job이 이미 존재할 경우, 기존 Job과 Trigger를 삭제하고
     * 새로운 정보로 다시 생성하여 스케줄을 업데이트합니다. {@code JobDataMap}을 통해
     * 실행될 Job에게 어떤 워크플로우를 실행해야 하는지 ID를 전달합니다.
     *
     * @param schedule Quartz에 등록할 스케줄 정보를 담은 도메인 모델 객체
     * @since v0.1.0
     */
    public void addOrUpdateSchedule(Schedule schedule) {
        try {
            JobKey jobKey = JobKey.jobKey("workflow-" + schedule.getWorkflowId());
            JobDetail jobDetail = JobBuilder.newJob(WorkflowTriggerJob.class)
                    .withIdentity(jobKey)
                    .withDescription("Workflow " + schedule.getWorkflowId() + " Trigger Job")
                    .usingJobData("workflowId", schedule.getWorkflowId())
                    .storeDurably()
                    .build();

            TriggerKey triggerKey = TriggerKey.triggerKey("trigger-for-workflow-" + schedule.getWorkflowId());
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(schedule.getCronExpression()))
                    .build();

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey); // 기존 Job 삭제 후 재생성 (업데이트)
            }
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Quartz 스케줄 등록/업데이트 완료: Workflow ID {}", schedule.getWorkflowId());
        } catch (SchedulerException e) {
            log.error("Quartz 스케줄 등록 실패: Workflow ID " + schedule.getWorkflowId(), e);
        }
    }

    /**
     * 지정된 워크플로우 ID와 연결된 Quartz 스케줄을 삭제합니다.
     *
     * @param workflowId 삭제할 스케줄에 연결된 워크플로우의 ID
     * @since v0.1.0
     */
    public void deleteSchedule(Long workflowId) {
        try {
            JobKey jobKey = JobKey.jobKey("workflow-" + workflowId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("Quartz 스케줄 삭제 완료: Workflow ID {}", workflowId);
            }
        } catch (SchedulerException e) {
            log.error("Quartz 스케줄 삭제 실패: Workflow ID " + workflowId, e);
        }
    }
}