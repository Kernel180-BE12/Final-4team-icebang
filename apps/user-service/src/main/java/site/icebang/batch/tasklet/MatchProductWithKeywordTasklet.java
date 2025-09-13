package site.icebang.batch.tasklet;

import java.util.List;
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
public class MatchProductWithKeywordTasklet implements Tasklet {

    public static final String MATCHED_PRODUCT_ID = "matchedProductId";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>> [Step 3] 상품-키워드 매칭 Task 실행 시작");

        ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        List<Long> productCandidates = (List<Long>) jobExecutionContext.get(SearchProductsFromMallTasklet.PRODUCT_CANDIDATES);

        if (productCandidates == null || productCandidates.isEmpty()) {
            log.warn(">>>> 이전 Step에서 전달된 상품 후보가 없습니다. Step 3을 건너뜁니다.");
            return RepeatStatus.FINISHED;
        }

        // TODO: 키워드와 상품 후보 목록 간의 매칭 점수를 계산하여 최적의 상품을 찾는 로직 구현
        // 예시: 첫 번째 상품이 가장 적합하다고 선택
        Long matchedProductId = productCandidates.get(0);

        jobExecutionContext.put(MATCHED_PRODUCT_ID, matchedProductId);
        log.info(">>>> 최종 매칭 상품 ID: {}, JobExecutionContext에 저장 완료", matchedProductId);

        log.info(">>>> [Step 3] 상품-키워드 매칭 Task 실행 완료");
        return RepeatStatus.FINISHED;
    }
}
