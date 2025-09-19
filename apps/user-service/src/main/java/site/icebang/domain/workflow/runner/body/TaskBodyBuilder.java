package site.icebang.domain.workflow.runner.body;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import site.icebang.domain.workflow.model.Task;

public interface TaskBodyBuilder {
    boolean supports(String taskName);
    ObjectNode build(Task task, Map<String, JsonNode> workflowContext);
}