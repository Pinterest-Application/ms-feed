package com.example.msfeed.service;

import com.example.msfeed.config.DiscoveryFeedProperties;
import com.example.msfeed.entity.Interaction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserTasteProfileBuilder {

    public static final int DIMENSION = 1152;

    private static final double MIN_NORM = 1e-9;

    private final DiscoveryFeedProperties properties;

    @Getter
    @RequiredArgsConstructor
    public static class TasteProfile {

        private final float[] queryVector;
        private final Set<UUID> seenPinIds;

        public boolean hasVector() {
            return queryVector != null;
        }
    }

    public Map<UUID, Double> aggregatePinScores(List<Interaction> interactions, Instant now) {
        double lambda = Math.log(2) / properties.getHalfLifeDays();
        Map<UUID, Double> scores = new HashMap<>();

        for (Interaction interaction : interactions) {
            if (interaction.getPinId() == null
                    || interaction.getInteractionType() == null
                    || interaction.getCreatedAt() == null) {
                continue;
            }
            double deltaDays = Math.max(0,
                    Duration.between(interaction.getCreatedAt(), now).toMillis() / (1000.0 * 60 * 60 * 24));
            double weight = interaction.getInteractionType().getWeight() * Math.exp(-lambda * deltaDays);
            scores.merge(interaction.getPinId(), weight, Double::sum);
        }

        double cap = properties.getMaxWeightPerPin();
        scores.replaceAll((pinId, score) -> Math.max(-cap, Math.min(cap, score)));
        return scores;
    }

    public TasteProfile build(Map<UUID, Double> pinScores, Map<UUID, float[]> embeddings) {
        double[] pos = new double[DIMENSION];
        double[] neg = new double[DIMENSION];

        for (Map.Entry<UUID, Double> entry : pinScores.entrySet()) {
            float[] vector = embeddings.get(entry.getKey());
            double score = entry.getValue();
            if (vector == null || vector.length != DIMENSION || score == 0) {
                continue;
            }
            double[] target = score > 0 ? pos : neg;
            double weight = Math.abs(score);
            for (int d = 0; d < DIMENSION; d++) {
                target[d] += weight * vector[d];
            }
        }

        Set<UUID> seenPinIds = Set.copyOf(pinScores.keySet());
        if (!normalize(pos)) {
            return new TasteProfile(null, seenPinIds);
        }

        if (normalize(neg)) {
            double beta = properties.getNegativeBeta();
            for (int d = 0; d < DIMENSION; d++) {
                pos[d] -= beta * neg[d];
            }
            if (!normalize(pos)) {
                return new TasteProfile(null, seenPinIds);
            }
        }

        float[] query = new float[DIMENSION];
        for (int d = 0; d < DIMENSION; d++) {
            query[d] = (float) pos[d];
        }
        return new TasteProfile(query, seenPinIds);
    }

    private static boolean normalize(double[] vector) {
        double sumSquares = 0;
        for (double value : vector) {
            sumSquares += value * value;
        }
        double norm = Math.sqrt(sumSquares);
        if (norm < MIN_NORM) {
            return false;
        }
        for (int d = 0; d < vector.length; d++) {
            vector[d] /= norm;
        }
        return true;
    }
}
