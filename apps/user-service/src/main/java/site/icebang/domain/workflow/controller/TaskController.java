package site.icebang.domain.workflow.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.workflow.dto.TaskDto;
import site.icebang.domain.workflow.service.TaskService;

@RestController
@RequestMapping("/v0/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;

  @PostMapping
  public ResponseEntity<Map<String, Object>> createTask(@RequestBody TaskDto dto) {
    TaskDto created = taskService.createTask(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", created));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getTask(@PathVariable Long id) {
    TaskDto task = taskService.findById(id);
    if (task == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false));
    }
    return ResponseEntity.ok(Map.of("success", true, "data", task));
  }
}
