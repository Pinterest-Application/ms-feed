package com.example.msfeed.session;

import com.example.msfeed.dto.FeedResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface FeedSessionStore {

    FeedSession create(UUID userId);

    Optional<FeedSession> find(UUID sessionId);

    int append(FeedSession session, List<FeedResponse> batch, int maxSize);

    void markExhausted(FeedSession session);

    List<FeedResponse> slice(UUID sessionId, int offset, int size);

    Set<UUID> servedPinIds(UUID sessionId);
}
