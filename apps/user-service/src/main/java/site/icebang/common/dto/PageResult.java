package site.icebang.common.dto;

import java.util.List;

import lombok.Data;

@Data
public class PageResult<T> {
  private List<T> data;
  private int total;
  private int current;
  private int pageSize;
  private int totalPages;
  private boolean hasNext;
  private boolean hasPrevious;

  public PageResult(List<T> data, int total, int current, int pageSize) {
    this.data = data;
    this.total = total;
    this.current = current;
    this.pageSize = pageSize;
    this.totalPages = (int) Math.ceil((double) total / pageSize);
    this.hasNext = current < totalPages;
    this.hasPrevious = current > 1;
  }

  public static <T> PageResult<T> of(List<T> data, int total, int current, int pageSize) {
    return new PageResult<>(data, total, current, pageSize);
  }
}
