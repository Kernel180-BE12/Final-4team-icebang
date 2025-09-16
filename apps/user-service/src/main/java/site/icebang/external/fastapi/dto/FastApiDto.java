package site.icebang.external.fastapi.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

@Deprecated
/** FastAPI 서버와 통신하기 위한 DTO 클래스 모음. Java의 record를 사용하여 불변 데이터 객체를 간결하게 정의합니다. */
public final class FastApiDto {

  // --- 1. 네이버 키워드 추출 ---
  public record RequestNaverSearch(
      @JsonProperty("job_id") int jobId,
      @JsonProperty("schedule_id") int scheduleId,
      @JsonProperty("schedule_his_id") Integer scheduleHisId,
      String tag,
      String category,
      @JsonProperty("start_date") String startDate,
      @JsonProperty("end_date") String endDate) {}

  public record ResponseNaverSearch(
      String status,
      String category,
      String keyword,
      @JsonProperty("total_keyword") Map<Integer, String> totalKeyword) {}

  // --- 2. 상품 검색 ---
  public record RequestSsadaguSearch(
      @JsonProperty("job_id") int jobId,
      @JsonProperty("schedule_id") int scheduleId,
      @JsonProperty("schedule_his_id") Integer scheduleHisId,
      String keyword) {}

  public record ResponseSsadaguSearch(
      String status,
      String keyword,
      @JsonProperty("search_results") List<Map<String, Object>> searchResults) {}

  // --- 3. 상품 매칭 ---
  public record RequestSsadaguMatch(
      @JsonProperty("job_id") int jobId,
      @JsonProperty("schedule_id") int scheduleId,
      @JsonProperty("schedule_his_id") Integer scheduleHisId,
      String keyword,
      @JsonProperty("search_results") List<Map<String, Object>> searchResults) {}

  public record ResponseSsadaguMatch(
      String status,
      String keyword,
      @JsonProperty("matched_products") List<Map<String, Object>> matchedProducts) {}

  // --- 4. 상품 유사도 ---
  public record RequestSsadaguSimilarity(
      @JsonProperty("job_id") int jobId,
      @JsonProperty("schedule_id") int scheduleId,
      @JsonProperty("schedule_his_id") Integer scheduleHisId,
      String keyword,
      @JsonProperty("matched_products") List<Map<String, Object>> matchedProducts,
      @JsonProperty("search_results") List<Map<String, Object>> searchResults) {}

  public record ResponseSsadaguSimilarity(
      String status,
      String keyword,
      @JsonProperty("selected_product") Map<String, Object> selectedProduct,
      String reason) {}

  // --- 5. 상품 크롤링 ---
  public record RequestSsadaguCrawl(
      @JsonProperty("job_id") int jobId,
      @JsonProperty("schedule_id") int scheduleId,
      @JsonProperty("schedule_his_id") Integer scheduleHisId,
      String tag,
      @JsonProperty("product_url") String productUrl) {}

  public record ResponseSsadaguCrawl(
      String status,
      String tag,
      @JsonProperty("product_url") String productUrl,
      @JsonProperty("product_detail") Map<String, Object> productDetail,
      @JsonProperty("crawled_at") String crawledAt) {}

  // --- 6. 블로그 콘텐츠 생성 ---
  public record RequestBlogCreate(
      @JsonProperty("job_id") int jobId,
      @JsonProperty("schedule_id") int scheduleId,
      @JsonProperty("schedule_his_id") Integer scheduleHisId) {}

  public record ResponseBlogCreate(String status) {}

  // --- 7. 블로그 발행 ---
  public record RequestBlogPublish(
      @JsonProperty("job_id") int jobId,
      @JsonProperty("schedule_id") int scheduleId,
      @JsonProperty("schedule_his_id") Integer scheduleHisId,
      String tag,
      @JsonProperty("blog_id") String blogId,
      @JsonProperty("blog_pw") String blogPw,
      @JsonProperty("post_title") String postTitle,
      @JsonProperty("post_content") String postContent,
      @JsonProperty("post_tags") List<String> postTags) {}

  public record ResponseBlogPublish(String status, Map<String, Object> metadata) {}
}
