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
public class CrawlSelectedProductTasklet implements Tasklet {

    public static final String CRAWLED_PRODUCT_DATA = "crawledProductData";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>> [Step 5] 최종 선택 상품 크롤링 Task 실행 시작");

        ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        Long selectedProductId = (Long) jobExecutionContext.get(MatchProductWithKeywordTasklet.MATCHED_PRODUCT_ID);

        if (selectedProductId == null) {
            log.warn(">>>> 이전 Step에서 전달된 최종 상품 ID가 없습니다. Step 5를 건너뜁니다.");
            return RepeatStatus.FINISHED;
        }

        log.info(">>>> 상품 ID {}에 대한 상세 정보 크롤링 시작...", selectedProductId);
        // TODO: FastAPI 워커 등을 호출하여 해당 상품의 상세 페이지를 크롤링하는 로직 구현
        // 예시: 크롤링 결과로 상품 상세 정보를 Map 형태로 가져왔다고 가정
        Map<String, Object> crawledData = Map.of(
                "productId", selectedProductId,
                "productName", "초경량 캠핑 릴렉스 체어",
                "price", 35000,
                "description", "매우 편안하고 가벼운 캠핑 의자입니다."
        );

        jobExecutionContext.put(CRAWLED_PRODUCT_DATA, crawledData);
        log.info(">>>> 크롤링된 데이터: {}, JobExecutionContext에 저장 완료", crawledData);

        log.info(">>>> [Step 5] 최종 선택 상품 크롤링 Task 실행 완료");
        return RepeatStatus.FINISHED;
    }
}