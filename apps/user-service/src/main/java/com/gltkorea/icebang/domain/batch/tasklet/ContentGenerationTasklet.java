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
public class ContentGenerationTasklet implements Tasklet {

  // private final ContentService contentService; // 비즈니스 로직을 담은 서비스
  // private final FastApiClient fastApiClient; // FastAPI 통신을 위한 클라이언트

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    log.info(">>>> [Step 2] ContentGenerationTasklet executed.");

    // --- 핵심: JobExecutionContext에서 이전 Step의 결과물 가져오기 ---
    ExecutionContext jobExecutionContext =
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

    // KeywordExtractionTasklet이 저장한 "extractedKeywordIds" Key로 데이터 조회
    List<Long> keywordIds = (List<Long>) jobExecutionContext.get("extractedKeywordIds");

    if (keywordIds == null || keywordIds.isEmpty()) {
      log.warn(">>>> No keyword IDs found from previous step. Skipping content generation.");
      return RepeatStatus.FINISHED;
    }

    log.info(">>>> Received Keyword IDs for content generation: {}", keywordIds);

    // TODO: 1. 전달받은 키워드 ID 목록으로 DB에서 상세 정보 조회
    // TODO: 2. 각 키워드/상품 정보에 대해 외부 AI 서비스(FastAPI/LangChain)를 호출하여 콘텐츠 생성을 요청
    // TODO: 3. 생성된 콘텐츠를 DB에 저장

    log.info(">>>> [Step 2] ContentGenerationTasklet finished.");
    return RepeatStatus.FINISHED;
  }
}
