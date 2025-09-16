package site.icebang.common.dto;

import java.util.List;
import java.util.function.Supplier;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
    calculatePagination();
  }

  // 페이징 계산 로직 분리
  private void calculatePagination() {
    this.totalPages = total > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    this.hasNext = current < totalPages;
    this.hasPrevious = current > 1;
  }

  // 기존 of 메서드
  public static <T> PageResult<T> of(List<T> data, int total, int current, int pageSize) {
    return new PageResult<>(data, total, current, pageSize);
  }

  // PageParams를 받는 of 메서드
  public static <T> PageResult<T> of(List<T> data, int total, PageParams pageParams) {
    return new PageResult<>(data, total, pageParams.getCurrent(), pageParams.getPageSize());
  }

  // 함수형 인터페이스를 활용한 from 메서드 (트랜잭션 내에서 실행)
  public static <T> PageResult<T> from(
      PageParams pageParams, Supplier<List<T>> dataSupplier, Supplier<Integer> countSupplier) {
    List<T> data = dataSupplier.get();
    int total = countSupplier.get();
    return new PageResult<>(data, total, pageParams.getCurrent(), pageParams.getPageSize());
  }

  // 빈 페이지 결과 생성
  public static <T> PageResult<T> empty(PageParams pageParams) {
    return new PageResult<>(List.of(), 0, pageParams.getCurrent(), pageParams.getPageSize());
  }

  // 빈 페이지 결과 생성 (기본값)
  public static <T> PageResult<T> empty() {
    return new PageResult<>(List.of(), 0, 1, 10);
  }

  // 데이터가 있는지 확인
  public boolean hasData() {
    return data != null && !data.isEmpty();
  }

  // 첫 번째 페이지인지 확인
  public boolean isFirstPage() {
    return current == 1;
  }

  // 마지막 페이지인지 확인
  public boolean isLastPage() {
    return current == totalPages;
  }
}
