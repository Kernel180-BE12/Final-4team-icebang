package site.icebang.domain.workflow.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // MyBatis가 객체를 생성하기 위해 필요
public class Task {

    private Long id;
    private String name;

    /**
     * Task의 타입 (예: "HTTP", "SPRING_BATCH")
     * 이 타입에 따라 TaskRunner가 선택됩니다.
     */
    private String type;

    /**
     * Task 실행에 필요한 파라미터 (JSON)
     * 예: {"url": "http://...", "method": "POST", "body": {...}}
     */
    private JsonNode parameters;

}