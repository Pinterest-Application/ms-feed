package com.example.msfeed.repository;

import com.example.msfeed.config.DiscoveryFeedProperties;
import com.example.msfeed.dto.FeedResponse;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeedCandidateRepository {

    private static final String COLUMNS = "id, pin_id, media_url, media_width, media_height, created_at";

    private static final String SIMILAR_SQL = """
            WITH candidates AS MATERIALIZED (
                SELECT %1$s, embedding <=> ? AS distance
                FROM ms_feed.feed
                WHERE embedding IS NOT NULL
                  AND NOT (pin_id = ANY(?))
                ORDER BY distance
                LIMIT ?
            )
            SELECT %1$s FROM candidates ORDER BY distance
            """.formatted(COLUMNS);

    private static final String RECENT_SQL = """
            SELECT %s
            FROM ms_feed.feed
            WHERE NOT (pin_id = ANY(?))
            ORDER BY created_at DESC
            LIMIT ?
            """.formatted(COLUMNS);

    private static final RowMapper<FeedResponse> ROW_MAPPER = (rs, rowNum) -> FeedResponse.builder()
            .id(rs.getObject("id", UUID.class))
            .pinId(rs.getObject("pin_id", UUID.class))
            .mediaUrl(rs.getString("media_url"))
            .mediaWidth(rs.getObject("media_width", Integer.class))
            .mediaHeight(rs.getObject("media_height", Integer.class))
            .createdAt(rs.getObject("created_at", OffsetDateTime.class))
            .build();

    private final JdbcTemplate jdbcTemplate;
    private final DiscoveryFeedProperties properties;

    @Transactional(readOnly = true)
    public List<FeedResponse> findSimilar(float[] queryVector, Collection<UUID> excludedPinIds, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        jdbcTemplate.execute("SET LOCAL hnsw.ef_search = " + Math.max(properties.getHnswEfSearch(), limit));
        if (properties.isHnswIterativeScan()) {
            jdbcTemplate.execute("SET LOCAL hnsw.iterative_scan = relaxed_order");
        }

        return jdbcTemplate.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(SIMILAR_SQL);
            ps.setObject(1, new PGvector(queryVector));
            ps.setArray(2, connection.createArrayOf("uuid", excludedPinIds.toArray()));
            ps.setInt(3, limit);
            return ps;
        }, ROW_MAPPER);
    }

    @Transactional(readOnly = true)
    public List<FeedResponse> findRecent(Collection<UUID> excludedPinIds, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return jdbcTemplate.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(RECENT_SQL);
            ps.setArray(1, connection.createArrayOf("uuid", excludedPinIds.toArray()));
            ps.setInt(2, limit);
            return ps;
        }, ROW_MAPPER);
    }
}
