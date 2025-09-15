package site.icebang.batch.tasklet;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import site.icebang.batch.common.JobContextKeys;
import site.icebang.external.fastapi.adapter.FastApiAdapter;
import site.icebang.external.fastapi.dto.FastApiDto.RequestSsadaguSearch;
import site.icebang.external.fastapi.dto.FastApiDto.ResponseSsadaguSearch;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchProductsFromMallTasklet implements Tasklet {

    private final FastApiAdapter fastApiAdapter;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>> [Step 2] 상품 검색 Tasklet 실행 시작");

        ExecutionContext jobExecutionContext = getJobExecutionContext(chunkContext);
        String keyword = (String) jobExecutionContext.get(JobContextKeys.EXTRACTED_KEYWORD);

        if (keyword == null) {
            throw new RuntimeException("이전 Step에서 키워드를 전달받지 못했습니다.");
        }

        RequestSsadaguSearch request = new RequestSsadaguSearch(1, 1, null, keyword);
        ResponseSsadaguSearch response = fastApiAdapter.requestSsadaguProductSearch(request);

        if (response == null || !"200".equals(response.status())) {
            throw new RuntimeException("FastAPI 상품 검색에 실패했습니다.");
        }
        List<Map<String, Object>> searchResults = response.searchResults();
        log.info(">>>> FastAPI로부터 검색된 상품 {}개", searchResults.size());

        jobExecutionContext.put(JobContextKeys.SEARCHED_PRODUCTS, searchResults);

        log.info(">>>> [Step 2] 상품 검색 Tasklet 실행 완료");
        return RepeatStatus.FINISHED;
    }

    private ExecutionContext getJobExecutionContext(ChunkContext chunkContext) {
        return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
    }
}

