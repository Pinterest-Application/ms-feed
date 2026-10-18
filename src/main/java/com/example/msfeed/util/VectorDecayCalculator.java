package com.example.msfeed.util;

import com.example.msfeed.entity.Feed;
import com.example.msfeed.entity.Interaction;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class VectorDecayCalculator {

    private static final int DIMENSION = 1152;
    private static final double HALF_LIFE_DAYS = 7.0;
    private static final double LAMBDA = Math.log(2) / HALF_LIFE_DAYS;

    public static float[] calculateUserProfileVector(List<Interaction> interactions, List<Feed> interactedPins) {
        Map<UUID, float[]> pinVectorMap = interactedPins.stream()
                .collect(Collectors.toMap(Feed::getPinId, Feed::getEmbedding));

        float[] centroid = new float[DIMENSION];
        Instant now = Instant.now();

        for (Interaction interaction : interactions) {
            float[] pinVector = pinVectorMap.get(interaction.getPinId());
            if (pinVector == null) continue;

            double deltaDays = Duration.between(interaction.getCreatedAt(), now).toMillis() / (1000.0 * 60 * 60 * 24);
            if (deltaDays < 0) deltaDays = 0;

            double timeWeight = interaction.getWeight() * Math.exp(-LAMBDA * deltaDays);

            for (int d = 0; d < DIMENSION; d++) {
                centroid[d] += (float) (pinVector[d] * timeWeight);
            }
        }

        double sumSquares = 0.0;
        for (float val : centroid) {
            sumSquares += val * val;
        }
        float norm = (float) Math.sqrt(sumSquares);

        if (norm > 0) {
            for (int d = 0; d < DIMENSION; d++) {
                centroid[d] /= norm;
            }
        }

        return centroid;
    }

    public static String toPgVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
