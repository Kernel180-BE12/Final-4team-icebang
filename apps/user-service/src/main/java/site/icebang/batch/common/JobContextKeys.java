package site.icebang.batch.common;

/**
 * Spring Batch의 JobExecutionContext에서 Step 간 데이터 공유를 위해 사용되는
 * Key들을 상수로 정의하는 인터페이스.
 * 모든 Tasklet은 이 인터페이스를 참조하여 데이터의 일관성을 유지합니다.
 */
public interface JobContextKeys {

    String EXTRACTED_KEYWORD = "extractedKeyword";
    String SEARCHED_PRODUCTS = "searchedProducts";
    String MATCHED_PRODUCTS = "matchedProducts";
    String SELECTED_PRODUCT = "selectedProduct";
    String CRAWLED_PRODUCT_DETAIL = "crawledProductDetail";
    String GENERATED_CONTENT = "generatedContent";
}