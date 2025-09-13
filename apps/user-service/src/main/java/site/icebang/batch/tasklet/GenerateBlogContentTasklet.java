package site.icebang.batch.tasklet;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateBlogContentTasklet implements Tasklet {

    public static final String BLOG_CONTENT = "blogContent";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>> [Step 6] 블로그 콘텐츠 생성 Task 실행 시작");

        ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        Map<String, Object> productData = (Map<String, Object>) jobExecutionContext.get(CrawlSelectedProductTasklet.CRAWLED_PRODUCT_DATA);

        if (productData == null) {
            log.warn(">>>> 이전 Job에서 전달된 상품 정보가 없습니다. Step 6을 건너뜁니다.");
            return RepeatStatus.FINISHED;
        }

        log.info(">>>> 상품 정보 '{}'를 기반으로 콘텐츠 생성 시작...", productData.get("productName"));
        // TODO: FastAPI/LangChain 등을 호출하여 상품 정보로 블로그 원고를 생성하는 로직 구현
        // 예시: AI가 생성한 블로그 콘텐츠
        Map<String, String> blogContent = Map.of(
                "title", "오늘의 추천! " + productData.get("productName"),
                "body", productData.get("description") + " 이 상품은 정말 최고입니다! 가격은 " + productData.get("price") + "원!"
        );

        jobExecutionContext.put(BLOG_CONTENT, blogContent);
        log.info(">>>> 생성된 블로그 콘텐츠: {}, JobExecutionContext에 저장 완료", blogContent.get("title"));

        log.info(">>>> [Step 6] 블로그 콘텐츠 생성 Task 실행 완료");
        return RepeatStatus.FINISHED;
    }
}