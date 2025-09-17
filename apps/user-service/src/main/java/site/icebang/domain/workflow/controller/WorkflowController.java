package site.icebang.domain.workflow.controller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import org.springframework.web.client.RestTemplate;
import site.icebang.common.dto.ApiResponse;
import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.domain.workflow.dto.WorkflowCardDto;
import site.icebang.domain.workflow.model.Job;
import site.icebang.domain.workflow.model.Task;
import site.icebang.domain.workflow.mapper.JobMapper;
import site.icebang.domain.workflow.service.WorkflowExecutionService;
import site.icebang.domain.workflow.service.WorkflowService;

@RestController
@RequestMapping("/v0/workflows")
@RequiredArgsConstructor
public class WorkflowController {
  private final WorkflowService workflowService;
  private final WorkflowExecutionService workflowExecutionService;
  private final JobMapper jobMapper;

  @Autowired
  private RestTemplate restTemplate;

  @GetMapping("")
  public ApiResponse<PageResult<WorkflowCardDto>> getWorkflowList(
      @ModelAttribute PageParams pageParams) {
    PageResult<WorkflowCardDto> result = workflowService.getPagedResult(pageParams);
    return ApiResponse.success(result);
  }

  @PostMapping("/{workflowId}/run")
  public ResponseEntity<Void> runWorkflow(@PathVariable Long workflowId) {
    // HTTP 요청/응답 스레드를 블로킹하지 않도록 비동기 실행
    CompletableFuture.runAsync(() -> workflowExecutionService.executeWorkflow(workflowId));
    return ResponseEntity.accepted().build();

  }

  @GetMapping("/debug/{workflowId}")
  public ResponseEntity<?> debugWorkflow(@PathVariable Long workflowId) {
    try {
      // 1. 워크플로우 존재 확인
      StringBuilder debug = new StringBuilder();
      debug.append("=== 워크플로우 디버깅 ===\n");
      debug.append("워크플로우 ID: ").append(workflowId).append("\n\n");
      // 2. Job 목록 확인
      // 1. Job 목록 조회
      List<Job> jobs = jobMapper.findJobsByWorkflowId(workflowId);
      debug.append("Job 개수: ").append(jobs.size()).append("\n");

      for (Job job : jobs) {
        debug.append("- Job ID: ").append(job.getId())
                .append(", 이름: ").append(job.getName()).append("\n");

        // 2. 각 Job의 Task 목록 조회
        List<Task> tasks = jobMapper.findTasksByJobId(job.getId());
        debug.append("  Task 개수: ").append(tasks.size()).append("\n");

        for (Task task : tasks) {
          debug.append("  - Task ID: ").append(task.getId())
                  .append(", 이름: ").append(task.getName())
                  .append(", 타입: ").append(task.getType()).append("\n");
          debug.append("    파라미터: ").append(task.getParameters()).append("\n");
        }
      }
      // 3. Task 목록 확인
      // 4. 실제 실행
      return ResponseEntity.ok(debug.toString());

    } catch (Exception e) {
      return ResponseEntity.status(500).body("에러: " + e.getMessage() +
              "\n스택트레이스: " + Arrays.toString(e.getStackTrace()));
    }
  }
}
