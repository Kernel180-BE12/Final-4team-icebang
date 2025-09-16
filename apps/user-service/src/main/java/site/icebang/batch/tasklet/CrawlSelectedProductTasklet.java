package site.icebang.batch.tasklet;

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
import site.icebang.external.fastapi.dto.FastApiDto.RequestSsadaguCrawl;
import site.icebang.external.fastapi.dto.FastApiDto.ResponseSsadaguCrawl;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlSelectedProductTasklet implements Tasklet {

  private final FastApiAdapter fastApiAdapter;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    // log.info(">>>> [Step 5] 최종 상품 크롤링 Tasklet 실행 시작");

    ExecutionContext jobExecutionContext = getJobExecutionContext(chunkContext);
    Map<String, Object> selectedProduct =
        (Map<String, Object>) jobExecutionContext.get(JobContextKeys.SELECTED_PRODUCT);

    if (selectedProduct == null || !selectedProduct.containsKey("link")) {
      throw new RuntimeException("크롤링할 상품 URL이 없습니다.");
    }
    String productUrl = (String) selectedProduct.get("link");

    RequestSsadaguCrawl request = new RequestSsadaguCrawl(1, 1, null, "detail", productUrl);
    ResponseSsadaguCrawl response = fastApiAdapter.requestProductCrawl(request);

    if (response == null || !"200".equals(response.status())) {
      throw new RuntimeException("FastAPI 상품 크롤링에 실패했습니다.");
    }

    Map<String, Object> productDetail = response.productDetail();
    log.info(">>>> FastAPI로부터 크롤링된 상품 상세 정보 획득");

    jobExecutionContext.put(JobContextKeys.CRAWLED_PRODUCT_DETAIL, productDetail);

    // log.info(">>>> [Step 5] 최종 상품 크롤링 Tasklet 실행 완료");
    return RepeatStatus.FINISHED;
  }

  private ExecutionContext getJobExecutionContext(ChunkContext chunkContext) {
    return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
  }
}
