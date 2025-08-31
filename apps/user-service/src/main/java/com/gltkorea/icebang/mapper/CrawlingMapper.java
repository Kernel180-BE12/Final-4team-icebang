package com.gltkorea.icebang.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.gltkorea.icebang.dto.ProductCrawlingData;

@Mapper
public interface CrawlingMapper {
  List<ProductCrawlingData> findProductsToCrawl(Map<String, Object> parameters);

  void updateCrawledProduct(ProductCrawlingData productData);
}
