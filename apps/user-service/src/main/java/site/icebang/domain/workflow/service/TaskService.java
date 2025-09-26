package site.icebang.domain.workflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import site.icebang.domain.workflow.dto.TaskDto;
import site.icebang.domain.workflow.mapper.TaskMapper;

@Service
@RequiredArgsConstructor
public class TaskService {

  private final TaskMapper taskMapper;

  @Transactional
  public TaskDto createTask(TaskDto dto) {
    if (dto.getName() == null || dto.getName().isBlank()) {
      throw new IllegalArgumentException("task name is required");
    }
    taskMapper.insertTask(dto);
    return taskMapper.findTaskById(dto.getId());
  }

  public TaskDto findById(Long id) {
    return taskMapper.findTaskById(id);
  }
}
