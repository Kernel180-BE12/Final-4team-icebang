package com.gltkorea.icebang.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data // Lombok
public class ProductCrawlingData {
    private Long id;
    private String name;
    private String urlToCrawl; // DB 컬럼명은 url_to_crawl
    private int price;
    private String stockStatus; // DB 컬럼명은 stock_status
    private LocalDateTime lastCrawledAt; // DB 컬럼명은 last_crawled_at
}