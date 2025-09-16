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
import site.icebang.external.fastapi.dto.FastApiDto.RequestBlogPublish;
import site.icebang.external.fastapi.dto.FastApiDto.ResponseBlogPublish;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublishBlogPostTasklet implements Tasklet {

  private final FastApiAdapter fastApiAdapter;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    // log.info(">>>> [Step 7] 블로그 발행 Tasklet 실행 시작");

    ExecutionContext jobExecutionContext = getJobExecutionContext(chunkContext);
    Map<String, Object> content =
        (Map<String, Object>) jobExecutionContext.get(JobContextKeys.GENERATED_CONTENT);

    // TODO: UserConfig 등에서 실제 블로그 정보(ID, PW)를 가져와야 함
    String blogId = "my_blog_id";
    String blogPw = "my_blog_password";

    RequestBlogPublish request =
        new RequestBlogPublish(
            1,
            1,
            null,
            "naver",
            blogId,
            blogPw,
            (String) content.get("title"),
            (String) content.get("content"),
            (List<String>) content.get("tags"));

    ResponseBlogPublish response = fastApiAdapter.requestBlogPost(request);

    if (response == null || !"200".equals(response.status())) {
      throw new RuntimeException("FastAPI 블로그 발행에 실패했습니다.");
    }

    log.info(">>>> FastAPI를 통해 블로그 발행 성공: {}", response.metadata());

    // log.info(">>>> [Step 7] 블로그 발행 Tasklet 실행 완료");
    return RepeatStatus.FINISHED;
  }

  private ExecutionContext getJobExecutionContext(ChunkContext chunkContext) {
    return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
  }
}
