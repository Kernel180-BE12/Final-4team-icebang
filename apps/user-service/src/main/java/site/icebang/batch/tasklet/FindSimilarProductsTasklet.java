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
import site.icebang.external.fastapi.dto.FastApiDto.RequestSsadaguSimilarity;
import site.icebang.external.fastapi.dto.FastApiDto.ResponseSsadaguSimilarity;

@Slf4j
@Component
@RequiredArgsConstructor
public class FindSimilarProductsTasklet implements Tasklet {

  private final FastApiAdapter fastApiAdapter;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    // log.info(">>>> [Step 4] 상품 유사도 분석 Tasklet 실행 시작");

    ExecutionContext jobExecutionContext = getJobExecutionContext(chunkContext);
    String keyword = (String) jobExecutionContext.get(JobContextKeys.EXTRACTED_KEYWORD);
    List<Map<String, Object>> matchedProducts =
        (List<Map<String, Object>>) jobExecutionContext.get(JobContextKeys.MATCHED_PRODUCTS);
    List<Map<String, Object>> searchResults =
        (List<Map<String, Object>>) jobExecutionContext.get(JobContextKeys.SEARCHED_PRODUCTS);

    RequestSsadaguSimilarity request =
        new RequestSsadaguSimilarity(1, 1, null, keyword, matchedProducts, searchResults);
    ResponseSsadaguSimilarity response = fastApiAdapter.requestProductSimilarity(request);

    if (response == null || !"200".equals(response.status())) {
      throw new RuntimeException("FastAPI 상품 유사도 분석에 실패했습니다.");
    }

    Map<String, Object> selectedProduct = response.selectedProduct();
    log.info(">>>> FastAPI로부터 최종 선택된 상품: {}", selectedProduct.get("title"));

    jobExecutionContext.put(JobContextKeys.SELECTED_PRODUCT, selectedProduct);

    // log.info(">>>> [Step 4] 상품 유사도 분석 Tasklet 실행 완료");
    return RepeatStatus.FINISHED;
  }

  private ExecutionContext getJobExecutionContext(ChunkContext chunkContext) {
    return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
  }
}
