package site.icebang.domain.job.mapper;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import site.icebang.domain.job.model.Job;

@Mapper
public interface JobMapper {
    Optional<Job> findById(Long id);
}