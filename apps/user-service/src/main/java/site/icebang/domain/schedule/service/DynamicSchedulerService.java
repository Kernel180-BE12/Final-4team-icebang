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
import site.icebang.domain.workflow.service.WorkflowExecutionService;

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