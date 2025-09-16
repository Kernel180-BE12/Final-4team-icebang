package site.icebang.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import site.icebang.batch.common.JobContextKeys;
import site.icebang.external.fastapi.adapter.FastApiAdapter;
import site.icebang.external.fastapi.dto.FastApiDto.RequestNaverSearch;
import site.icebang.external.fastapi.dto.FastApiDto.ResponseNaverSearch;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractTrendKeywordTasklet implements Tasklet {

  private final FastApiAdapter fastApiAdapter;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    // log.info(">>>> [Step 1] 키워드 추출 Tasklet 실행 시작");

    RequestNaverSearch request =
        new RequestNaverSearch(1, 1, null, "naver", "50000000", null, null);
    ResponseNaverSearch response = fastApiAdapter.requestNaverKeywordSearch(request);

    if (response == null || !"200".equals(response.status())) {
      throw new RuntimeException("FastAPI로부터 키워드를 추출하는 데 실패했습니다.");
    }
    String extractedKeyword = response.keyword();
    log.info(">>>> FastAPI로부터 추출된 키워드: {}", extractedKeyword);

    ExecutionContext jobExecutionContext = getJobExecutionContext(chunkContext);
    // 다른 클래스의 상수를 직접 참조하는 대신 공용 인터페이스의 키를 사용
    jobExecutionContext.put(JobContextKeys.EXTRACTED_KEYWORD, extractedKeyword);

    // log.info(">>>> [Step 1] 키워드 추출 Tasklet 실행 완료");
    return RepeatStatus.FINISHED;
  }

  private ExecutionContext getJobExecutionContext(ChunkContext chunkContext) {
    return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
  }
}
