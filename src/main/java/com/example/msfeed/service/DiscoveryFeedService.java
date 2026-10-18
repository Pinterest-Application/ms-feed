package com.example.msfeed.service;

import com.example.msfeed.config.CacheConfig;
import com.example.msfeed.dto.FeedResponse;
import com.example.msfeed.entity.Feed;
import com.example.msfeed.entity.Interaction;
import com.example.msfeed.repository.FeedRepository;
import com.example.msfeed.repository.InteractionRepository;
import com.example.msfeed.util.VectorDecayCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryFeedService {

    private final FeedRepository feedRepository;
    private final InteractionRepository interactionRepository;

    public Page<FeedResponse> getDiscoveryFeed(UUID userId, Pageable pageable) {
        List<FeedResponse> fullFeed = getOrComputeTop50Feed(userId);

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<FeedResponse> pageContent;
        if (fullFeed.size() <= startItem) {
            pageContent = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, fullFeed.size());
            pageContent = fullFeed.subList(startItem, toIndex);
        }

        return new PageImpl<>(pageContent, pageable, fullFeed.size());
    }

    @Cacheable(value = CacheConfig.DISCOVERY_FEED_CACHE, key = "#userId")
    public List<FeedResponse> getOrComputeTop50Feed(UUID userId) {
        log.info("Computing fresh 1152-d discovery feed for userId: {}", userId);

        List<Interaction> interactions = interactionRepository.findByUserId(userId);
        List<Feed> resultPins;

        if (interactions.isEmpty()) {
            resultPins = feedRepository.findTopRecentPins(50);
        } else {
            List<UUID> pinIds = interactions.stream().map(Interaction::getPinId).distinct().toList();
            List<Feed> interactedPins = feedRepository.findAllByPinIdIn(pinIds);

            // 1152 ölçülü profil vektorunun hesablanması
            float[] userCentroid = VectorDecayCalculator.calculateUserProfileVector(interactions, interactedPins);
            String pgVectorQuery = VectorDecayCalculator.toPgVectorString(userCentroid);

            resultPins = feedRepository.findTopSimilarPins(pgVectorQuery, pinIds, 50);

            // Əgər oxşar pinlərin sayı 50-dən az olarsa, qalanını yeni pinlərlə tamamlayır
            if (resultPins.size() < 50) {
                int deficit = 50 - resultPins.size();
                List<Feed> backupPins = feedRepository.findTopRecentPins(deficit);
                resultPins.addAll(backupPins);
            }
        }

        return resultPins.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private FeedResponse mapToResponse(Feed feed) {
        return FeedResponse.builder()
                .id(feed.getId())
                .pinId(feed.getPinId())
                .mediaUrl(feed.getMediaUrl())
                .mediaWidth(feed.getMediaWidth())
                .mediaHeight(feed.getMediaHeight())
                .createdAt(feed.getCreatedAt())
                .build();
    }
}