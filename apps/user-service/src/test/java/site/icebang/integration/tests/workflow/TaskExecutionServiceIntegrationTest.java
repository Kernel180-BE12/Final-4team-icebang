package site.icebang.integration.tests.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientException;
import site.icebang.domain.workflow.model.TaskRun;
import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.runner.TaskRunner;
import site.icebang.domain.workflow.runner.fastapi.FastApiTaskRunner;
import site.icebang.domain.workflow.service.TaskExecutionService;
import site.icebang.integration.setup.support.IntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TaskExecutionService의 재시도 로직에 대한 통합 테스트 클래스입니다.
 * 실제 Spring 컨텍스트를 로드하여 RetryTemplate 기반의 재시도 기능이 정상 동작하는지 검증합니다.
 */
public class TaskExecutionServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private TaskExecutionService taskExecutionService;

    @MockitoBean(name = "fastapiTaskRunner")
    private FastApiTaskRunner mockFastApiTaskRunner;

    @Test
    @DisplayName("Task 실행이 3번 모두 실패하면, 재시도 로그가 3번 기록되고 최종 FAILED 결과를 반환해야 한다")
    void executeWithRetry_shouldLogRetries_andFail_afterAllRetries() {
        // given
        Task testTask = new Task(1L, "테스트 태스크", "FastAPI", null, null, null, null);
        TaskRun testTaskRun = new TaskRun();
        ObjectNode testRequestBody = new ObjectMapper().createObjectNode();

        // Mock Runner가 호출될 때마다 예외를 던지도록 설정
        when(mockFastApiTaskRunner.execute(any(Task.class), any(TaskRun.class), any(ObjectNode.class)))
                .thenThrow(new RestClientException("Connection failed"));

        // when
        // RetryTemplate이 적용된 실제 서비스를 호출
        TaskRunner.TaskExecutionResult finalResult =
                taskExecutionService.executeWithRetry(testTask, testTaskRun, testRequestBody);

        // then
        // 1. Runner의 execute 메소드가 RetryTemplate 정책에 따라 3번 호출되었는지 검증
        verify(mockFastApiTaskRunner, times(3))
                .execute(any(Task.class), any(TaskRun.class), any(ObjectNode.class));

        // 2. RecoveryCallback이 반환한 최종 결과가 FAILED인지 검증
        assertThat(finalResult.isFailure()).isTrue();
        assertThat(finalResult.message()).contains("최대 재시도 횟수 초과");
    }
}