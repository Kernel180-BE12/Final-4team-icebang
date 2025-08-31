package com.gltkorea.icebang.mapper;

import com.gltkorea.icebang.dto.ProductCrawlingData;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface CrawlingMapper {
    List<ProductCrawlingData> findProductsToCrawl(Map<String, Object> parameters);
    void updateCrawledProduct(ProductCrawlingData productData);
}