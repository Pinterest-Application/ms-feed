package com.example.msfeed.consumer;

import com.example.msfeed.entity.Feed;
import com.example.msfeed.entity.dto.PinImageEmbeddingEvent;
import com.example.msfeed.repository.FeedRepository;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedEmbeddingConsumer {

    private final FeedRepository feedRepository;

    @Transactional
    @KafkaListener(topics = "pin-image-embedding-topic", groupId = "feed-service-group")
    public void consumeEmbeddingEvent(PinImageEmbeddingEvent event) {
        log.info("Received embedding message from Kafka for pinId: {}", event.getPinId());

        if (event.getEmbedding() == null || event.getEmbedding().length == 0) {
            log.warn("Embedding array is missing or empty for pinId: {}. Skipping persistence.", event.getPinId());
            return;
        }

        Feed feed = feedRepository.findByPinId(event.getPinId())
                .orElseGet(() -> Feed.builder()
                        .pinId(event.getPinId())
                        .build());

        feed.setMediaUrl(event.getMediaUrl());
        feed.setMediaWidth(event.getMediaWidth());
        feed.setMediaHeight(event.getMediaHeight());
        feed.setEmbedding(event.getEmbedding());
        feedRepository.save(feed);
        log.info("Feed entity persisted successfully with 1152-d vector for pinId: {}", event.getPinId());
    }

}
