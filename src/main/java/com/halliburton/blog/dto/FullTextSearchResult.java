package com.halliburton.blog.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class FullTextSearchResult {
    private String schema;
    private String table;
    private String columns;
    private String keys;
    private BigDecimal score;
}
