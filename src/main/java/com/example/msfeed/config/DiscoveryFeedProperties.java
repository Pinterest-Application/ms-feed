package com.example.msfeed.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "feed.discovery")
public class DiscoveryFeedProperties {

    private int batchSize = 100;

    private int maxSessionSize = 1000;

    private Duration sessionTtl = Duration.ofMinutes(30);

    private int maxInteractions = 500;

    private double halfLifeDays = 7.0;

    private double maxWeightPerPin = 1.5;

    private double negativeBeta = 0.3;

    private double explorationRatio = 0.2;

    private int hnswEfSearch = 200;

    private boolean hnswIterativeScan = true;

    private Duration maxClientTimestampSkew = Duration.ofDays(1);
}
