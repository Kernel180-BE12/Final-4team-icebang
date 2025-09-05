package com.gltkorea.icebang.domain.batch.tasklet;

import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordExtractionTasklet implements Tasklet {

  // private final TrendKeywordService trendKeywordService; // 비즈니스 로직을 담은 서비스

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    log.info(">>>> [Step 1] KeywordExtractionTasklet executed.");

    // TODO: 1. DB에서 카테고리 정보 조회
    // TODO: 2. 외부 API 또는 내부 로직을 통해 트렌드 키워드 추출
    // TODO: 3. 추출된 키워드를 DB에 저장

    // --- 핵심: 다음 Step에 전달할 데이터 생성 ---
    // 예시: 새로 생성된 키워드 ID 목록을 가져왔다고 가정
    List<Long> extractedKeywordIds = List.of(1L, 2L, 3L); // 실제로는 DB 저장 후 반환된 ID 목록
    log.info(">>>> Extracted Keyword IDs: {}", extractedKeywordIds);

    // --- 핵심: JobExecutionContext에 결과물 저장 ---
    // JobExecution 전체에서 공유되는 컨텍스트를 가져옵니다.
    ExecutionContext jobExecutionContext =
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

    // "extractedKeywordIds" 라는 Key로 데이터 저장
    jobExecutionContext.put("extractedKeywordIds", extractedKeywordIds);

    log.info(">>>> [Step 1] KeywordExtractionTasklet finished.");
    return RepeatStatus.FINISHED;
  }
}
