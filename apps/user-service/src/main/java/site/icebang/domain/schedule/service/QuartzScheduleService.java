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
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Quartz 스케줄 등록/업데이트 완료: Workflow ID {}", schedule.getWorkflowId());
        } catch (SchedulerException e) {
            log.error("Quartz 스케줄 등록 실패", e);
        }
    }

    public void deleteSchedule(Long workflowId) {
        // ... (삭제 로직)
    }
}