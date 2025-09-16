//package site.icebang.domain.schedule.service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ScheduledFuture;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.context.ApplicationContext;
//import org.springframework.scheduling.TaskScheduler;
//import org.springframework.scheduling.support.CronTrigger;
//import org.springframework.stereotype.Service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import site.icebang.domain.job.mapper.JobMapper;
//import site.icebang.domain.job.model.Job;
//import site.icebang.domain.schedule.model.Schedule;
//import site.icebang.domain.mapping.mapper.WorkflowJobMapper;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class DynamicSchedulerService {
//
//  private final TaskScheduler taskScheduler;
//  private final JobLauncher jobLauncher;
//  private final ApplicationContext applicationContext;
//  private final WorkflowJobMapper workflowJobMapper;
//  private final JobMapper jobMapper;
//
//  private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
//
//  public void register(Schedule schedule) {
//    // 1. 스케줄에 연결된 워크플로우에 속한 Job ID 목록을 조회합니다.
//    // execution_order에 따라 정렬됩니다.
//    List<Long> jobIds = workflowJobMapper.findJobIdsByWorkflowId(schedule.getWorkflowId());
//
//    if (jobIds.isEmpty()) {
//      log.error("No jobs found for workflowId: {}. Cannot register scheduleId: {}",
//              schedule.getWorkflowId(), schedule.getId());
//      return;
//    }
//
//    // TODO: 현재는 워크플로우의 첫 번째 Job만 실행하도록 구현되어 있습니다.
//    // 향후 여러 Job을 순차적으로 실행하거나, 별도의 Workflow 실행 서비스를 호출하는 방식으로 확장해야 합니다.
//    Long firstJobId = jobIds.get(0);
//    Job job = jobMapper.findById(firstJobId)
//            .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + firstJobId));
//
//    // 2. Job의 이름을 Spring Batch Job Bean 이름으로 사용하여 컨텍스트에서 Job을 찾습니다.
//    String jobBeanName = job.getName();
//    org.springframework.batch.core.Job jobToRun;
//    try {
//      jobToRun = applicationContext.getBean(jobBeanName, org.springframework.batch.core.Job.class);
//    } catch (Exception e) {
//      log.error("Cannot find Spring Batch Job bean with name '{}' for scheduleId: {}", jobBeanName, schedule.getId(), e);
//      return;
//    }
//
//    Runnable runnable = () -> {
//      try {
//        // 3. JobParameters에 동적인 값을 추가하여 매 실행이 고유하도록 보장합니다.
//        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
//        paramsBuilder.addString("runDateTime", LocalDateTime.now().toString());
//        paramsBuilder.addLong("scheduleId", schedule.getId());
//        paramsBuilder.addLong("workflowId", schedule.getWorkflowId());
//
//        jobLauncher.run(jobToRun, paramsBuilder.toJobParameters());
//      } catch (Exception e) {
//        log.error("Error running scheduled job for scheduleId: {}", schedule.getId(), e);
//      }
//    };
//
//    CronTrigger trigger = new CronTrigger(schedule.getCronExpression());
//    ScheduledFuture<?> future = taskScheduler.schedule(runnable, trigger);
//    scheduledTasks.put(schedule.getId(), future);
//    log.info(">>>> Schedule registered: id={}, jobBeanName={}, cron={}", schedule.getId(), jobBeanName, schedule.getCronExpression());
//  }
//
//  public void remove(Long scheduleId) {
//    ScheduledFuture<?> future = scheduledTasks.get(scheduleId);
//    if (future != null) {
//      future.cancel(true);
//      scheduledTasks.remove(scheduleId);
//      log.info(">>>> Schedule removed: id={}", scheduleId);
//    }
//  }
//}


package site.icebang.domain.schedule.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.icebang.domain.schedule.model.Schedule;
import site.icebang.workflow.service.WorkflowExecutionService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicSchedulerService {

  private final TaskScheduler taskScheduler;
  private final WorkflowExecutionService workflowExecutionService;

  private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

  public void register(Schedule schedule) {
    // 스케줄 실행 시 WorkflowExecutionService를 호출하는 Runnable 생성
    Runnable runnable = () -> {
      try {
        log.debug("Triggering workflow execution for scheduleId: {}", schedule.getId());
        // 실제 워크플로우 실행은 WorkflowExecutionService에 위임 (비동기 호출)
        workflowExecutionService.execute(schedule.getWorkflowId(), "SCHEDULE", schedule.getId());
      } catch (Exception e) {
        // Async 예외는 기본적으로 처리되지 않으므로 여기서 로그를 남기는 것이 중요
        log.error("Failed to submit workflow execution for scheduleId: {}", schedule.getId(), e);
      }
    };

    CronTrigger trigger = new CronTrigger(schedule.getCronExpression());
    ScheduledFuture<?> future = taskScheduler.schedule(runnable, trigger);

    // 기존에 등록된 스케줄이 있다면 취소하고 새로 등록 (업데이트 지원)
    ScheduledFuture<?> oldFuture = scheduledTasks.put(schedule.getId(), future);
    if (oldFuture != null) {
      oldFuture.cancel(false);
    }

    log.info(">>>> Schedule registered/updated: id={}, workflowId={}, cron='{}'",
            schedule.getId(), schedule.getWorkflowId(), schedule.getCronExpression());
  }

  public void remove(Long scheduleId) {
    ScheduledFuture<?> future = scheduledTasks.remove(scheduleId);
    if (future != null) {
      future.cancel(true); // true: 실행 중인 태스크를 인터럽트
      log.info(">>>> Schedule removed: id={}", scheduleId);
    } else {
      log.warn(">>>> Attempted to remove a schedule that was not found in the scheduler: id={}", scheduleId);
    }
  }
}