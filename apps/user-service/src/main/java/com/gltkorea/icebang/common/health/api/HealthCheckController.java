package com.gltkorea.icebang.common.health.api;

import com.gltkorea.icebang.common.health.service.FastApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthCheckController {

    private final FastApiClient fastApiClient;

    /**
     * Spring Boot와 FastAPI 서버 간의 연결 상태를 확인하는 헬스 체크 API
     * @return FastAPI 서버로부터의 응답
     */
    @GetMapping("/ping")
    public ResponseEntity<String> pingFastApi() {
        String result = fastApiClient.ping();

        if (result.startsWith("ERROR")) {
            // FastAPI 연결 실패 시 503 Service Unavailable 상태 코드와 함께 에러 메시지 반환
            return ResponseEntity.status(503).body(result);
        }

        // 성공 시 200 OK 상태 코드와 함께 FastAPI로부터 받은 응답("PONG" 등) 반환
        return ResponseEntity.ok(result);
    }
}