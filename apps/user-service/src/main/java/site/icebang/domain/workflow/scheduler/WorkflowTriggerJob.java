package site.icebang.domain.workflow.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import site.icebang.domain.workflow.service.WorkflowExecutionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowTriggerJob extends QuartzJobBean {
    private final WorkflowExecutionService workflowExecutionService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        Long workflowId = context.getJobDetail().getJobDataMap().getLong("workflowId");
        log.info("Quartz가 WorkflowTriggerJob을 실행합니다. WorkflowId={}", workflowId);
        workflowExecutionService.executeWorkflow(workflowId);
    }
}