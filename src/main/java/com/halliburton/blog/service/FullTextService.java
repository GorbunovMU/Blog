package com.halliburton.blog.service;

import com.halliburton.blog.dto.FullTextSearchResult;
import org.h2.fulltext.FullText;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FullTextService {
    public static final int SEARCH_RESULT_LIMIT = 0;
    public static final int SEARCH_RESULT_OFFSET = 0;
    public static final int SCHEMA_INDEX = 1;
    public static final int TABLE_INDEX = 2;
    public static final int COLUMNS_INDEX = 3;
    public static final int KEYS_INDEX = 4;
    public static final int SCORE_INDEX = 5;

    private final DataSource dataSource;

    public FullTextService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<FullTextSearchResult> search(String keyword) throws SQLException {
        List<FullTextSearchResult> results = new ArrayList<>();

        ResultSet resultSet = FullText.searchData(
                dataSource.getConnection(),
                keyword,
                SEARCH_RESULT_LIMIT,
                SEARCH_RESULT_OFFSET);
        while (resultSet.next()) {
            String schemaName = resultSet.getString(SCHEMA_INDEX);
            String tableName = resultSet.getString(TABLE_INDEX);
            Object[] columns = (Object[]) resultSet.getArray(COLUMNS_INDEX).getArray();
            String column = (String) columns[0];
            Object[] keys = (Object[]) resultSet.getArray(KEYS_INDEX).getArray();
            String key = (String) keys[0];
            BigDecimal score = resultSet.getBigDecimal(SCORE_INDEX);

            results.add(
                    FullTextSearchResult.builder()
                            .schema(schemaName)
                            .table(tableName)
                            .columns(column)
                            .keys(key)
                            .score(score)
                            .build());
        }

        return results;
    }
}
