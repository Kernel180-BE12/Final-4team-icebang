package site.icebang.common.dto;

import lombok.Data;

@Data
public class PageParams {
  private int current = 1;
  private int pageSize = 10;
  private String search;
  private String[] sorters;
  private String[] filters;

  // 계산된 offset
  public int getOffset() {
    return (current - 1) * pageSize;
  }

  public boolean hasSearch() {
    return search != null && !search.trim().isEmpty();
  }

  public boolean hasSorters() {
    return sorters != null && sorters.length > 0;
  }
}
