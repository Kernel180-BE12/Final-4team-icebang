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
import site.icebang.external.fastapi.dto.FastApiDto.RequestBlogCreate;
import site.icebang.external.fastapi.dto.FastApiDto.ResponseBlogCreate;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateBlogContentTasklet implements Tasklet {

  private final FastApiAdapter fastApiAdapter;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    // log.info(">>>> [Step 6] 블로그 콘텐츠 생성 Tasklet 실행 시작");

    ExecutionContext jobExecutionContext = getJobExecutionContext(chunkContext);
    Map<String, Object> productDetail =
        (Map<String, Object>) jobExecutionContext.get(JobContextKeys.CRAWLED_PRODUCT_DETAIL);

    // TODO: productDetail을 기반으로 LLM에 전달할 프롬프트 생성
    RequestBlogCreate request = new RequestBlogCreate(1, 1, null);
    ResponseBlogCreate response = fastApiAdapter.requestBlogCreation(request);

    if (response == null || !"200".equals(response.status())) {
      throw new RuntimeException("FastAPI 블로그 콘텐츠 생성에 실패했습니다.");
    }

    // TODO: 실제 생성된 콘텐츠를 response로부터 받아와야 함 (현재는 더미 데이터)
    Map<String, Object> generatedContent =
        Map.of(
            "title", "엄청난 상품을 소개합니다! " + productDetail.get("title"),
            "content", "이 상품은 정말... 좋습니다. 상세 정보: " + productDetail.toString(),
            "tags", List.of("상품리뷰", "최고"));
    log.info(">>>> FastAPI로부터 블로그 콘텐츠 생성 완료");

    jobExecutionContext.put(JobContextKeys.GENERATED_CONTENT, generatedContent);

    // log.info(">>>> [Step 6] 블로그 콘텐츠 생성 Tasklet 실행 완료");
    return RepeatStatus.FINISHED;
  }

  private ExecutionContext getJobExecutionContext(ChunkContext chunkContext) {
    return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
  }
}
