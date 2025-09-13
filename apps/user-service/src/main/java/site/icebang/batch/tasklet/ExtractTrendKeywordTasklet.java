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
public class ExtractTrendKeywordTasklet implements Tasklet {

    public static final String KEYWORD_LIST = "keywordList";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>> [Step 1] 트렌드 키워드 추출 Task 실행 시작");

        // TODO: 실제 네이버 트렌드 등에서 키워드를 추출하는 로직 구현
        // 예시: "캠핑 의자"라는 키워드 목록을 추출했다고 가정
        List<String> keywordList = List.of("캠핑 의자", "경량 체어", "릴렉스 체어");

        // JobExecution 전체에서 공유되는 ExecutionContext를 가져옴
        ExecutionContext jobExecutionContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        // 다음 Step으로 전달하기 위해 추출된 키워드 목록을 저장
        jobExecutionContext.put(KEYWORD_LIST, keywordList);
        log.info(">>>> 추출된 키워드: {}, JobExecutionContext에 저장 완료", keywordList);

        log.info(">>>> [Step 1] 트렌드 키워드 추출 Task 실행 완료");
        return RepeatStatus.FINISHED;
    }
}
