package site.icebang.domain.workflow.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import site.icebang.common.dto.PageParams;
import site.icebang.domain.workflow.dto.WorkflowCardDto;
import site.icebang.domain.workflow.model.Workflow;

@Mapper
public interface WorkflowMapper {
  Optional<Workflow> findById(Long id);

  List<WorkflowCardDto> selectWorkflowList(PageParams pageParams);

  int selectWorkflowCount(PageParams pageParams);
}
