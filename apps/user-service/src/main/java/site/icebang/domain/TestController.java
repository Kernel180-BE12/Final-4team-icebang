package site.icebang.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import site.icebang.common.dto.ApiResponse;

@RestController
@RequestMapping("/v0/hi")
@Slf4j
public class TestController {
  private static final Logger workflowLogger = LoggerFactory.getLogger("WORKFLOW_HISTORY");

  @GetMapping("")
  public ApiResponse<String> test() {
    log.info("@@");
    //        MDC.put("traceId", UUID.randomUUID().toString());
    MDC.put("sourceId", "o1");
    MDC.put("executionType", "WORKFLOW");
    //        MDC.put("sourceId", "test-controller");

    // 이 로그는 DB에 저장됨
    workflowLogger.info("SLF4J로 찍은 워크플로우 로그");

    MDC.clear();
    return ApiResponse.success("hi");
  }
}
