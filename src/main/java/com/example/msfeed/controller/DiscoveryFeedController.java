package com.example.msfeed.controller;

import com.example.msfeed.dto.FeedResponse;
import com.example.msfeed.service.DiscoveryFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DiscoveryFeedController {

    private final DiscoveryFeedService discoveryFeedService;

    @GetMapping("/discovery")
    public ResponseEntity<Page<FeedResponse>> getDiscoveryFeed(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID userId = UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
        Pageable pageable = PageRequest.of(page, size);

        Page<FeedResponse> feedPage = discoveryFeedService.getDiscoveryFeed(userId, pageable);
        return ResponseEntity.ok(feedPage);
    }
}
