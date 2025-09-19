package site.icebang.domain.workflow.runner.body;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.workflow.model.Task;

@Component
@RequiredArgsConstructor
public class BlogPublishBodyBuilder implements TaskBodyBuilder {

    private final ObjectMapper objectMapper;
    private static final String TASK_NAME = "블로그 발행 태스크";
    private static final String RAG_SOURCE_TASK = "블로그 RAG 생성 태스크";

    @Override
    public boolean supports(String taskName) {
        return TASK_NAME.equals(taskName);
    }

    @Override
    public ObjectNode build(Task task, Map<String, JsonNode> workflowContext) {
        ObjectNode body = objectMapper.createObjectNode();

        // RAG에서 생성된 블로그 콘텐츠 가져오기
        Optional.ofNullable(workflowContext.get(RAG_SOURCE_TASK))
                .ifPresent(ragResult -> {
                    JsonNode data = ragResult.path("data");

                    // 제목, 내용, 태그 설정
                    Optional.ofNullable(data.path("title"))
                            .filter(node -> !node.isMissingNode())
                            .ifPresent(titleNode -> body.set("post_title", titleNode));

                    Optional.ofNullable(data.path("content"))
                            .filter(node -> !node.isMissingNode())
                            .ifPresent(contentNode -> body.set("post_content", contentNode));

                    Optional.ofNullable(data.path("tags"))
                            .filter(node -> !node.isMissingNode())
                            .ifPresent(tagsNode -> body.set("post_tags", tagsNode));
                });

        body.put("tag", "tistory");
        body.put("blog_id", "fair_05@nate.com");
        body.put("blog_pw", "kdyn2641*");

        return body;
    }
}