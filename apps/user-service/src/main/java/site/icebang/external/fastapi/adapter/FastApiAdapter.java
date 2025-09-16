package site.icebang.external.fastapi.adapter;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import site.icebang.external.fastapi.dto.FastApiDto.*;
import site.icebang.global.config.properties.FastApiProperties;

/** FastAPI 서버와의 통신을 전담하는 어댑터 클래스. 모든 외부 API 호출은 이 클래스를 통해 이루어집니다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiAdapter {

  private final RestTemplate restTemplate;
  private final FastApiProperties properties;

  /** TASK 1: 네이버 키워드 추출을 FastAPI에 요청합니다. */
  public ResponseNaverSearch requestNaverKeywordSearch(RequestNaverSearch request) {
    String url = properties.getUrl() + "/keyword/search";
    log.info("Requesting to FastAPI [POST {}]", url);
    try {
      return restTemplate.postForObject(url, request, ResponseNaverSearch.class);
    } catch (RestClientException e) {
      log.error("Failed to call FastAPI keyword search API. Error: {}", e.getMessage());
      // TODO: 비즈니스 요구사항에 맞는 예외 처리 (재시도, 기본값 반환, 특정 예외 던지기 등)
      return null;
    }
  }

  /** TASK 2: 싸다구몰 상품 검색을 FastAPI에 요청합니다. */
  public ResponseSsadaguSearch requestSsadaguProductSearch(RequestSsadaguSearch request) {
    String url = properties.getUrl() + "/product/search";
    log.info("Requesting to FastAPI [POST {}]", url);
    try {
      return restTemplate.postForObject(url, request, ResponseSsadaguSearch.class);
    } catch (RestClientException e) {
      log.error("Failed to call FastAPI product search API. Error: {}", e.getMessage());
      return null;
    }
  }

  /** TASK 3: 상품 매칭을 FastAPI에 요청합니다. */
  public ResponseSsadaguMatch requestProductMatch(RequestSsadaguMatch request) {
    String url = properties.getUrl() + "/product/match";
    log.info("Requesting to FastAPI [POST {}]", url);
    try {
      return restTemplate.postForObject(url, request, ResponseSsadaguMatch.class);
    } catch (RestClientException e) {
      log.error("Failed to call FastAPI product match API. Error: {}", e.getMessage());
      return null;
    }
  }

  /** TASK 4: 상품 유사도 분석을 FastAPI에 요청합니다. (메서드명 수정) */
  public ResponseSsadaguSimilarity requestProductSimilarity(RequestSsadaguSimilarity request) {
    String url = properties.getUrl() + "/product/similarity";
    log.info("Requesting to FastAPI [POST {}]", url);
    try {
      return restTemplate.postForObject(url, request, ResponseSsadaguSimilarity.class);
    } catch (RestClientException e) {
      log.error("Failed to call FastAPI product similarity API. Error: {}", e.getMessage());
      return null;
    }
  }

  /** TASK 5: 상품 상세 정보 크롤링을 FastAPI에 요청합니다. */
  public ResponseSsadaguCrawl requestProductCrawl(RequestSsadaguCrawl request) {
    String url = properties.getUrl() + "/product/crawl";
    log.info("Requesting to FastAPI [POST {}]", url);
    try {
      return restTemplate.postForObject(url, request, ResponseSsadaguCrawl.class);
    } catch (RestClientException e) {
      log.error("Failed to call FastAPI product crawl API. Error: {}", e.getMessage());
      return null;
    }
  }

  /** TASK 6: 블로그 콘텐츠 생성을 FastAPI에 요청합니다. */
  public ResponseBlogCreate requestBlogCreation(RequestBlogCreate request) {
    String url = properties.getUrl() + "/blog/rag/create";
    log.info("Requesting to FastAPI [POST {}]", url);
    try {
      return restTemplate.postForObject(url, request, ResponseBlogCreate.class);
    } catch (RestClientException e) {
      log.error("Failed to call FastAPI blog creation API. Error: {}", e.getMessage());
      return null;
    }
  }

  /** TASK 7: 블로그 발행을 FastAPI에 요청합니다. */
  public ResponseBlogPublish requestBlogPost(RequestBlogPublish request) {
    String url = properties.getUrl() + "/blog/publish";
    log.info("Requesting to FastAPI [POST {}]", url);
    try {
      return restTemplate.postForObject(url, request, ResponseBlogPublish.class);
    } catch (RestClientException e) {
      log.error("Failed to call FastAPI blog publish API. Error: {}", e.getMessage());
      return null;
    }
  }
}
