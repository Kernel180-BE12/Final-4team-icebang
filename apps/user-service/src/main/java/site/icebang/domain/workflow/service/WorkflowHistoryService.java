package site.icebang.domain.workflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;
import site.icebang.common.service.PageableService;
import site.icebang.domain.workflow.dto.WorkflowHistoryDTO;
import site.icebang.domain.workflow.mapper.WorkflowMapper;

@Service
@RequiredArgsConstructor
public class WorkflowHistoryService implements PageableService<WorkflowHistoryDTO> {

    private final WorkflowMapper workflowMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<WorkflowHistoryDTO> getPagedResult(PageParams pageParams) {

        return PageResult.from(
            pageParams,
            () -> workflowMapper.selectWorkflowHistoryList(pageParams),
            () -> workflowMapper.selectWorkflowHistoryCount(pageParams));
    }
}
