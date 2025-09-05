package com.gltkorea.icebang.domain.schedule.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import com.gltkorea.icebang.domain.schedule.model.Schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicSchedulerService {

  private final TaskScheduler taskScheduler;
  private final JobLauncher jobLauncher;
  private final ApplicationContext applicationContext;
  private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

  public void register(Schedule schedule) {
    // TODO: schedule.getWorkflowId()를 기반으로 실행할 Job의 이름을 DB에서 조회
    String jobName = "blogContentJob"; // 예시
    Job jobToRun = applicationContext.getBean(jobName, Job.class);

    Runnable runnable =
        () -> {
          try {
            JobParametersBuilder paramsBuilder = new JobParametersBuilder();
            paramsBuilder.addString("runAt", LocalDateTime.now().toString());
            paramsBuilder.addLong("scheduleId", schedule.getScheduleId());
            jobLauncher.run(jobToRun, paramsBuilder.toJobParameters());
          } catch (Exception e) {
            log.error(
                "Failed to run scheduled job for scheduleId: {}", schedule.getScheduleId(), e);
          }
        };

    CronTrigger trigger = new CronTrigger(schedule.getCronExpression());
    ScheduledFuture<?> future = taskScheduler.schedule(runnable, trigger);
    scheduledTasks.put(schedule.getScheduleId(), future);
    log.info(
        ">>>> Schedule registered: id={}, cron={}",
        schedule.getScheduleId(),
        schedule.getCronExpression());
  }

  public void remove(Long scheduleId) {
    ScheduledFuture<?> future = scheduledTasks.get(scheduleId);
    if (future != null) {
      future.cancel(true);
      scheduledTasks.remove(scheduleId);
      log.info(">>>> Schedule removed: id={}", scheduleId);
    }
  }
}
