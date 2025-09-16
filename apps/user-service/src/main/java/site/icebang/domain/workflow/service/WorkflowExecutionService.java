package site.icebang.workflow.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.icebang.domain.execution.mapper.JobRunMapper;
import site.icebang.domain.execution.mapper.WorkflowRunMapper;
import site.icebang.domain.execution.model.JobRun;
import site.icebang.domain.execution.model.WorkflowRun;
import site.icebang.domain.job.mapper.JobMapper;
import site.icebang.domain.job.model.Job;
import site.icebang.domain.mapping.mapper.WorkflowJobMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {

    private final JobLauncher jobLauncher;
    private final ApplicationContext applicationContext;
    private final WorkflowJobMapper workflowJobMapper;
    private final JobMapper jobMapper;
    private final WorkflowRunMapper workflowRunMapper;
    private final JobRunMapper jobRunMapper;

    /**
     * 워크플로우 실행을 비동기적으로 조율합니다.
     * 이 메서드 자체는 트랜잭션을 갖지 않으며, 내부적으로 호출하는 메서드들이
     * 각각 새로운 트랜잭션을 시작하여 실행 상태를 독립적으로 기록합니다.
     */
    @Async
    public void execute(Long workflowId, String triggerType, Long triggerId) {
        log.info("Starting workflow execution for workflowId: {}, triggered by: {}", workflowId, triggerType);

        // Step 1: 워크플로우 실행을 시작하고, 그 결과를 별도의 트랜잭션에 기록합니다.
        WorkflowRun workflowRun = this.initiateWorkflowExecution(workflowId, triggerType);

        try {
            // Step 2: 워크플로우에 속한 Job들을 순차적으로 실행합니다.
            List<Long> jobIds = workflowJobMapper.findJobIdsByWorkflowId(workflowId);
            if (jobIds.isEmpty()) {
                log.warn("No jobs found for workflowId: {}. Marking workflow as SUCCESS.", workflowId);
                this.finalizeWorkflowExecution(workflowRun.getId(), "SUCCESS");
                return;
            }

            AtomicInteger executionOrder = new AtomicInteger(1);
            for (Long jobId : jobIds) {
                // 각 Job의 실행과 상태 기록은 독립적인 트랜잭션으로 처리됩니다.
                this.executeJobInWorkflow(jobId, workflowRun.getId(), workflowRun.getTraceId(), executionOrder.getAndIncrement());
            }

            // Step 3: 모든 Job이 성공적으로 완료되면, 워크플로우의 최종 상태를 'SUCCESS'로 기록합니다.
            this.finalizeWorkflowExecution(workflowRun.getId(), "SUCCESS");
            log.info("Workflow execution successful for traceId: {}", workflowRun.getTraceId());

        } catch (Exception e) {
            // Step 4: Job 실행 중 예외가 발생하면, 워크플로우의 최종 상태를 'FAILED'로 기록합니다.
            log.error("Workflow execution failed for traceId: {}. Reason: {}", workflowRun.getTraceId(), e.getMessage(), e);
            this.finalizeWorkflowExecution(workflowRun.getId(), "FAILED");
        }
    }

    /**
     * 워크플로우 실행을 초기화하고 DB에 기록합니다.
     * 항상 새로운 트랜잭션에서 실행되어, 이 단계의 성공이 보장됩니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WorkflowRun initiateWorkflowExecution(Long workflowId, String triggerType) {
        WorkflowRun workflowRun = WorkflowRun.builder()
                .workflowId(workflowId)
                .traceId(UUID.randomUUID().toString())
                .status("PENDING")
                .triggerType(triggerType)
                .build();
        workflowRunMapper.save(workflowRun);

        // 상태를 'RUNNING'으로 변경하고 시작 시간을 기록합니다.
        workflowRun.setStartedAt(LocalDateTime.now());
        workflowRun.setStatus("RUNNING");
        workflowRunMapper.update(workflowRun);
        log.debug("Initiated workflow run with traceId: {}", workflowRun.getTraceId());
        return workflowRun;
    }

    /**
     * 워크플로우 실행을 최종 상태(SUCCESS/FAILED)로 업데이트합니다.
     * 항상 새로운 트랜잭션에서 실행되어, 실패 시에도 상태 기록이 롤백되지 않습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeWorkflowExecution(Long workflowRunId, String status) {
        WorkflowRun updatePayload = WorkflowRun.builder()
                .id(workflowRunId)
                .status(status)
                .finishedAt(LocalDateTime.now())
                .build();
        workflowRunMapper.update(updatePayload);
        log.debug("Finalized workflow run id: {} with status: {}", workflowRunId, status);
    }

    /**
     * 워크플로우 내의 단일 Job을 실행하고 그 결과를 DB에 기록합니다.
     * 항상 새로운 트랜잭션에서 실행되어, 각 Job의 실행 결과가 독립적으로 커밋됩니다.
     * 실패 시 예외를 던져 상위의 orchestrator가 인지하도록 합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeJobInWorkflow(Long jobId, Long workflowRunId, String traceId, int executionOrder) throws Exception {
        Job job = jobMapper.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found with id: " + jobId));

        JobRun jobRun = JobRun.builder()
                .workflowRunId(workflowRunId)
                .jobId(jobId)
                .status("PENDING")
                .executionOrder(executionOrder)
                .build();
        jobRunMapper.save(jobRun);

        try {
            org.springframework.batch.core.Job jobToRun = applicationContext.getBean(job.getName(), org.springframework.batch.core.Job.class);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("traceId", traceId)
                    .addLong("workflowRunId", workflowRunId)
                    .addLong("jobRunId", jobRun.getId())
                    .addString("runDateTime", LocalDateTime.now().toString())
                    .toJobParameters();

            jobRun.setStatus("RUNNING");
            jobRun.setStartedAt(LocalDateTime.now());
            jobRunMapper.update(jobRun);
            log.info("Executing job '{}' (id:{}) for workflow traceId: {}", job.getName(), jobId, traceId);

            JobExecution jobExecution = jobLauncher.run(jobToRun, jobParameters);

            if (jobExecution.getStatus().isUnsuccessful()) {
                throw new RuntimeException("Batch job '" + job.getName() + "' failed with status: " + jobExecution.getStatus());
            }

            jobRun.setStatus("SUCCESS");
            log.info("Successfully executed job '{}' (id:{})", job.getName(), jobId);

        } catch (Exception e) {
            jobRun.setStatus("FAILED");
            log.error("Failed to execute job '{}' (id:{}). Reason: {}", job.getName(), jobId, e.getMessage());
            throw e; // 워크플로우 전체를 실패 처리하기 위해 예외를 다시 던집니다.
        } finally {
            jobRun.setFinishedAt(LocalDateTime.now());
            jobRunMapper.update(jobRun);
        }
    }
}