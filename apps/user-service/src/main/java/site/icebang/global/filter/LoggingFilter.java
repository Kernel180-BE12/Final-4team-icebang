package site.icebang.global.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoggingFilter extends OncePerRequestFilter {

  public static final String TRACE_ID_HEADER = "X-Request-ID";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // 다른 시스템에서 이미 전달한 Trace ID가 있는지 확인
    String traceId = request.getHeader(TRACE_ID_HEADER);

    // 없다면 새로 생성 (요청의 시작점)
    if (traceId == null || traceId.isEmpty()) {
      traceId = UUID.randomUUID().toString();
    }

    MDC.put("traceId", traceId.substring(0, 8));

    // ⭐️ 요청 객체에 attribute로 traceId를 저장하여 컨트롤러 등에서 사용할 수 있게 함
    request.setAttribute("X-Request-ID", traceId);

    // 응답 헤더에 traceId를 넣어주면 클라이언트가 추적하기 용이
    response.setHeader(TRACE_ID_HEADER, traceId);

    filterChain.doFilter(request, response);
  }
}
