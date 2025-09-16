package site.icebang.batch.tasklet;

import java.util.List;
import java.util.Map;

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
import site.icebang.external.fastapi.dto.FastApiDto.RequestSsadaguMatch;
import site.icebang.external.fastapi.dto.FastApiDto.ResponseSsadaguMatch;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchProductWithKeywordTasklet implements Tasklet {

  private final FastApiAdapter fastApiAdapter;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    // log.info(">>>> [Step 3] 상품 매칭 Tasklet 실행 시작");

    ExecutionContext jobExecutionContext = getJobExecutionContext(chunkContext);
    String keyword = (String) jobExecutionContext.get(JobContextKeys.EXTRACTED_KEYWORD);
    List<Map<String, Object>> searchResults =
        (List<Map<String, Object>>) jobExecutionContext.get(JobContextKeys.SEARCHED_PRODUCTS);

    RequestSsadaguMatch request = new RequestSsadaguMatch(1, 1, null, keyword, searchResults);
    ResponseSsadaguMatch response = fastApiAdapter.requestProductMatch(request);

    if (response == null || !"200".equals(response.status())) {
      throw new RuntimeException("FastAPI 상품 매칭에 실패했습니다.");
    }

    List<Map<String, Object>> matchedProducts = response.matchedProducts();
    log.info(">>>> FastAPI로부터 매칭된 상품 {}개", matchedProducts.size());

    jobExecutionContext.put(JobContextKeys.MATCHED_PRODUCTS, matchedProducts);

    log.info(">>>> [Step 3] 상품 매칭 Tasklet 실행 완료");
    return RepeatStatus.FINISHED;
  }

  private ExecutionContext getJobExecutionContext(ChunkContext chunkContext) {
    return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
  }
}
