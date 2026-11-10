package com.example.msfeed.controller;

import com.example.msfeed.dto.DiscoveryFeedPage;
import com.example.msfeed.service.DiscoveryFeedService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<DiscoveryFeedPage> getDiscoveryFeed(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @Size(max = 200) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        DiscoveryFeedPage page = discoveryFeedService.getDiscoveryFeed(
                UUID.fromString(Objects.requireNonNull(jwt.getSubject())),
                cursor, size);
        return ResponseEntity.ok(page);
    }
}
