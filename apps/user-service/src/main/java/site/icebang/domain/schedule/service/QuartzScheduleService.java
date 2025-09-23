package site.icebang.domain.schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import site.icebang.domain.schedule.model.Schedule;
import site.icebang.domain.workflow.scheduler.WorkflowTriggerJob;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzScheduleService {
    private final Scheduler scheduler;

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