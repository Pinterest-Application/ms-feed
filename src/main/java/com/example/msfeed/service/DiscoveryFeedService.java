package com.example.msfeed.service;

import com.example.msfeed.config.DiscoveryFeedProperties;
import com.example.msfeed.dto.DiscoveryFeedPage;
import com.example.msfeed.dto.FeedResponse;
import com.example.msfeed.entity.Feed;
import com.example.msfeed.entity.Interaction;
import com.example.msfeed.repository.FeedCandidateRepository;
import com.example.msfeed.repository.FeedRepository;
import com.example.msfeed.repository.InteractionRepository;
import com.example.msfeed.service.UserTasteProfileBuilder.TasteProfile;
import com.example.msfeed.session.FeedCursor;
import com.example.msfeed.session.FeedSession;
import com.example.msfeed.session.FeedSessionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryFeedService {

    private final FeedRepository feedRepository;
    private final FeedCandidateRepository candidateRepository;
    private final InteractionRepository interactionRepository;
    private final UserTasteProfileBuilder profileBuilder;
    private final FeedSessionStore sessionStore;
    private final DiscoveryFeedProperties properties;

    public DiscoveryFeedPage getDiscoveryFeed(UUID userId, String cursor, int size) {
        FeedSession session;
        int offset;

        if (cursor == null || cursor.isBlank()) {
            session = sessionStore.create(userId);
            offset = 0;
        } else {
            FeedCursor decoded = FeedCursor.decode(cursor);
            FeedSession existing = sessionStore.find(decoded.getSessionId())
                    .filter(found -> found.getUserId().equals(userId))
                    .orElse(null);
            if (existing == null) {
                log.debug("Feed session {} not found or not owned by userId: {}, starting a new one",
                        decoded.getSessionId(), userId);
                session = sessionStore.create(userId);
                offset = 0;
            } else {
                session = existing;
                offset = decoded.getOffset();
            }
        }

        ensureCapacity(session, userId, offset + size);

        List<FeedResponse> items = sessionStore.slice(session.getId(), offset, size);
        int nextOffset = offset + items.size();
        boolean hasMore = nextOffset < session.getSize()
                || (!session.isExhausted() && session.getSize() < properties.getMaxSessionSize());
        String nextCursor = hasMore ? new FeedCursor(session.getId(), nextOffset).encode() : null;

        return DiscoveryFeedPage.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    private void ensureCapacity(FeedSession session, UUID userId, int required) {
        int target = Math.min(required, properties.getMaxSessionSize());
        while (session.getSize() < target && !session.isExhausted()) {
            Set<UUID> served = sessionStore.servedPinIds(session.getId());
            List<FeedResponse> batch = buildBatch(userId, served, properties.getBatchSize());
            int added = sessionStore.append(session, batch, properties.getMaxSessionSize());
            if (added == 0) {
                sessionStore.markExhausted(session);
            }
        }
    }

    private List<FeedResponse> buildBatch(UUID userId, Set<UUID> alreadyServed, int batchSize) {
        long startedAt = System.currentTimeMillis();

        List<Interaction> interactions = interactionRepository.findRecentByUserId(
                userId, Limit.of(properties.getMaxInteractions()));
        Map<UUID, Double> pinScores = profileBuilder.aggregatePinScores(interactions, Instant.now());

        Map<UUID, float[]> embeddings = pinScores.isEmpty()
                ? Map.of()
                : feedRepository.findAllByPinIdIn(pinScores.keySet()).stream()
                        .filter(feed -> feed.getEmbedding() != null)
                        .collect(Collectors.toMap(Feed::getPinId, Feed::getEmbedding, (first, second) -> first));

        TasteProfile profile = profileBuilder.build(pinScores, embeddings);

        Set<UUID> excluded = new HashSet<>(profile.getSeenPinIds());
        excluded.addAll(alreadyServed);

        List<FeedResponse> result;
        if (profile.hasVector()) {
            int exploreTarget = (int) Math.round(batchSize * properties.getExplorationRatio());
            List<FeedResponse> similar = candidateRepository.findSimilar(
                    profile.getQueryVector(), excluded, batchSize - exploreTarget);
            similar.forEach(item -> excluded.add(item.getPinId()));

            List<FeedResponse> explore = candidateRepository.findRecent(excluded, batchSize - similar.size());
            result = interleave(similar, explore, properties.getExplorationRatio());
        } else {
            result = candidateRepository.findRecent(excluded, batchSize);
        }

        log.info("Built discovery batch for userId: {} | interactions: {}, personalized: {}, items: {}, took: {} ms",
                userId, interactions.size(), profile.hasVector(), result.size(),
                System.currentTimeMillis() - startedAt);
        return result;
    }

    static List<FeedResponse> interleave(List<FeedResponse> main, List<FeedResponse> explore, double ratio) {
        if (explore.isEmpty()) {
            return main;
        }
        if (main.isEmpty()) {
            return explore;
        }
        if (ratio <= 0) {
            List<FeedResponse> result = new ArrayList<>(main);
            result.addAll(explore);
            return result;
        }

        int step = (int) Math.max(2, Math.round(1 / ratio));
        List<FeedResponse> result = new ArrayList<>(main.size() + explore.size());
        int mainIndex = 0;
        int exploreIndex = 0;

        while (mainIndex < main.size() || exploreIndex < explore.size()) {
            boolean exploreSlot = (result.size() + 1) % step == 0;
            if ((exploreSlot && exploreIndex < explore.size()) || mainIndex >= main.size()) {
                result.add(explore.get(exploreIndex++));
            } else {
                result.add(main.get(mainIndex++));
            }
        }
        return result;
    }
}
