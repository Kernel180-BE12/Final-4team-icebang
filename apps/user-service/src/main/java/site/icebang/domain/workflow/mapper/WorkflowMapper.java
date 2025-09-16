package site.icebang.domain.workflow.mapper;

import site.icebang.common.dto.PageParams;
import site.icebang.domain.workflow.dto.WorkflowCardDto;

import java.math.BigInteger;
import java.util.*;

public interface WorkflowMapper {
    List<WorkflowCardDto> selectWorkflowList(PageParams pageParams);

    int selectWorkflowCount(PageParams pageParams);

    WorkflowCardDto selectWorkflowById(BigInteger id);
}
