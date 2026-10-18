package com.example.msfeed.repository;

import com.example.msfeed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedRepository extends JpaRepository<Feed, UUID> {

    Optional<Feed> findByPinId(UUID pinId);

    List<Feed> findAllByPinIdIn(List<UUID> pinIds);

    // İstifadəçi vektoruna ən yaxın 50 pin (artıq baxılmış pinlər çıxılmaqla)
    @Query(value = """
        SELECT * FROM ms_feed.feed f
        WHERE f.pin_id NOT IN (:excludedPinIds)
        ORDER BY f.embedding <=> cast(:userVector as vector)
        LIMIT :limit
    """, nativeQuery = true)
    List<Feed> findTopSimilarPins(
            @Param("userVector") String userVector,
            @Param("excludedPinIds") List<UUID> excludedPinIds,
            @Param("limit") int limit
    );

    // Cold-start: Qarşılıqlı təsir olmadıqda ən son pinlər
    @Query(value = """
        SELECT * FROM ms_feed.feed f
        ORDER BY f.created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Feed> findTopRecentPins(@Param("limit") int limit);
}