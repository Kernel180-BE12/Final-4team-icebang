package site.icebang.common.service;

import site.icebang.common.dto.PageParams;
import site.icebang.common.dto.PageResult;

public interface PageableService<T> {
  PageResult<T> getPagedResult(PageParams pageParams);
}
