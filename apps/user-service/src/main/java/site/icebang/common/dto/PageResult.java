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
}
