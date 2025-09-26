package site.icebang.domain.workflow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.icebang.domain.workflow.dto.JobDto;
import site.icebang.domain.workflow.mapper.JobMapper;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobMapper jobMapper;

    @Transactional
    public JobDto createJob(JobDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("job name is required");
        }
        jobMapper.insertJob(dto);
        return jobMapper.findJobById(dto.getId());
    }

    public JobDto findById(Long id) {
        return jobMapper.findJobById(id);
    }
}
