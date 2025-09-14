package site.icebang.common.service;

import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import site.icebang.common.dto.ApiResponse;
import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;

@Service
public class PageService {

  public <T> ApiResponse<PageResult<T>> createPagedResponse(
      PageParams pageParams,
      Function<PageParams, List<T>> dataProvider,
      Function<PageParams, Integer> countProvider) {
    List<T> data = dataProvider.apply(pageParams);
    int total = countProvider.apply(pageParams);

    PageResult<T> pageResult =
        PageResult.of(data, total, pageParams.getCurrent(), pageParams.getPageSize());

    return ApiResponse.success(pageResult);
  }
}
