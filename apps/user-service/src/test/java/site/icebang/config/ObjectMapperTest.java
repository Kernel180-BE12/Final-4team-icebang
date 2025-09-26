package site.icebang.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import site.icebang.domain.workflow.dto.WorkflowRunDto;

@SpringBootTest
@ActiveProfiles("test-unit")
class ObjectMapperTest {

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldSerializeTimeWithZSuffix() throws Exception {
    // given
    WorkflowRunDto dto =
        WorkflowRunDto.builder()
            .id(1L)
            .workflowName("test-workflow")
            .startedAt("2024-01-01T10:00:00")
            .finishedAt("2024-01-01T10:30:00")
            .createdAt("2024-01-01T10:00:00")
            .build();

    // when
    String json = objectMapper.writeValueAsString(dto);

    // then
    System.out.println("Serialized JSON: " + json);
    assertThat(json).contains("2024-01-01T10:00:00Z");
  }

  @Test
  void shouldSerializeInstantWithZSuffix() throws Exception {
    // given
    Instant now = Instant.parse("2024-01-01T10:00:00Z");

    // when
    String json = objectMapper.writeValueAsString(now);

    // then
    System.out.println("Instant JSON: " + json);
    assertThat(json).contains("2024-01-01T10:00:00Z");
  }
}
