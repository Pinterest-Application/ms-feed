package com.example.msfeed.repository;

import com.example.msfeed.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedRepository extends JpaRepository<Feed, UUID> {
    Optional<Feed> findByPinId(UUID pinId);
}
