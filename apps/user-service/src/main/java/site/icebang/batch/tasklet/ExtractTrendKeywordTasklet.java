package site.icebang.batch.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import site.icebang.external.fastapi.adapter.FastApiAdapter;
import site.icebang.external.fastapi.dto.FastApiDto.RequestNaverSearch;
import site.icebang.external.fastapi.dto.FastApiDto.ResponseNaverSearch;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractTrendKeywordTasklet implements Tasklet {

    // ExecutionContext에서 사용할 데이터 Key
    public static final String EXTRACTED_KEYWORD = "extractedKeyword";

    private final FastApiAdapter fastApiAdapter;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>> [Step 1] 키워드 추출 Tasklet 실행 시작");

        // 1. FastAPI에 보낼 요청 DTO 생성 (실제 값은 JobParameters 등에서 동적으로 가져와야 함)
        RequestNaverSearch request = new RequestNaverSearch(1, 1, null, "naver", "50000000", null, null);

        // 2. FastApiAdapter를 통해 FastAPI 호출
        ResponseNaverSearch response = fastApiAdapter.requestNaverKeywordSearch(request);

        // 3. 응답 검증
        if (response == null || !"200".equals(response.status())) {
            throw new RuntimeException("FastAPI로부터 키워드를 추출하는 데 실패했습니다.");
        }
        String extractedKeyword = response.keyword();
        log.info(">>>> FastAPI로부터 추출된 키워드: {}", extractedKeyword);

        // 4. 다음 Step으로 전달하기 위해 결과를 JobExecutionContext에 저장
        ExecutionContext jobExecutionContext = getJobExecutionContext(chunkContext);
        jobExecutionContext.put(EXTRACTED_KEYWORD, extractedKeyword);

        log.info(">>>> [Step 1] 키워드 추출 Tasklet 실행 완료");
        return RepeatStatus.FINISHED;
    }

    private ExecutionContext getJobExecutionContext(ChunkContext chunkContext) {
        return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
    }
}

