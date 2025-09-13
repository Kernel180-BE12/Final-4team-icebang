package site.icebang.batch.tasklet;

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
public class FindSimilarProductsTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>> [Step 4] 유사 상품 탐색 Task 실행 시작");

        ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        Long matchedProductId = (Long) jobExecutionContext.get(MatchProductWithKeywordTasklet.MATCHED_PRODUCT_ID);

        if (matchedProductId == null) {
            log.warn(">>>> 이전 Step에서 전달된 매칭 상품 ID가 없습니다. Step 4를 건너뜁니다.");
            return RepeatStatus.FINISHED;
        }

        // TODO: 선택된 상품과 유사한 다른 상품들을 탐색하여 콘텐츠를 풍부하게 만드는 로직 구현
        log.info(">>>> 상품 ID {}에 대한 유사 상품 탐색 수행...", matchedProductId);

        log.info(">>>> [Step 4] 유사 상품 탐색 Task 실행 완료");
        return RepeatStatus.FINISHED;
    }
}
