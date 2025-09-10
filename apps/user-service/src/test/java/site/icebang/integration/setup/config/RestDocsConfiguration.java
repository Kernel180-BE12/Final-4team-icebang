package site.icebang.integration.setup.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.Preprocessors;

import com.fasterxml.jackson.databind.ObjectMapper;

@TestConfiguration
public class RestDocsConfiguration {

  @Bean
  public RestDocumentationResultHandler restDocumentationResultHandler() {
    return MockMvcRestDocumentation.document(
        "{class-name}/{method-name}",
        Preprocessors.preprocessRequest(
            Preprocessors.removeHeaders("Host", "Content-Length"), Preprocessors.prettyPrint()),
        Preprocessors.preprocessResponse(
            Preprocessors.removeHeaders("Content-Length", "Date", "Keep-Alive", "Connection"),
            Preprocessors.prettyPrint()));
  }

  @Bean
  public ObjectMapper testObjectMapper() {
    return new ObjectMapper();
  }
}
