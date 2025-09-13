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
public class SearchProductsFromMallTasklet implements Tasklet {

    public static final String PRODUCT_CANDIDATES = "productCandidates";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>> [Step 2] 쇼핑몰 상품 검색 Task 실행 시작");

        ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

        // 이전 Step에서 저장한 키워드 목록을 가져옴
        List<String> keywordList = (List<String>) jobExecutionContext.get(ExtractTrendKeywordTasklet.KEYWORD_LIST);

        if (keywordList == null || keywordList.isEmpty()) {
            log.warn(">>>> 이전 Step에서 전달된 키워드가 없습니다. Step 2를 건너뜁니다.");
            return RepeatStatus.FINISHED;
        }

        log.info(">>>> 키워드 '{}'(으)로 상품 검색 시작", keywordList.get(0));
        // TODO: FastAPI 워커 등을 호출하여 실제 쇼핑몰에서 상품 목록을 검색하는 로직 구현
        // 예시: 상품 후보 3개를 찾았다고 가정
        List<Long> productCandidates = List.of(1001L, 1002L, 1003L); // 상품 ID 목록

        // 다음 Step으로 전달하기 위해 후보 상품 목록을 저장
        jobExecutionContext.put(PRODUCT_CANDIDATES, productCandidates);
        log.info(">>>> 상품 후보 목록: {}, JobExecutionContext에 저장 완료", productCandidates);

        log.info(">>>> [Step 2] 쇼핑몰 상품 검색 Task 실행 완료");
        return RepeatStatus.FINISHED;
    }
}
