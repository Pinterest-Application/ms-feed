package com.example.msfeed.session;

import com.example.msfeed.config.DiscoveryFeedProperties;
import com.example.msfeed.dto.FeedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisStore implements FeedSessionStore {

    private static final String KEY_PREFIX = "feed:session:{";

    private static final RedisScript<List> FIND_SCRIPT = RedisScript.of("""
            local meta = redis.call('HMGET', KEYS[1], 'userId', 'exhausted')
            if not meta[1] then
                return {}
            end
            for i = 1, 3 do
                redis.call('PEXPIRE', KEYS[i], ARGV[1])
            end
            return {meta[1], meta[2], redis.call('LLEN', KEYS[2])}
            """, List.class);

    private static final RedisScript<List> APPEND_SCRIPT = RedisScript.of("""
            local maxSize = tonumber(ARGV[1])
            local added = 0
            for i = 3, #ARGV, 2 do
                if redis.call('LLEN', KEYS[2]) >= maxSize then
                    break
                end
                if redis.call('SADD', KEYS[3], ARGV[i]) == 1 then
                    redis.call('RPUSH', KEYS[2], ARGV[i + 1])
                    added = added + 1
                end
            end
            for i = 1, 3 do
                redis.call('PEXPIRE', KEYS[i], ARGV[2])
            end
            return {added, redis.call('LLEN', KEYS[2])}
            """, List.class);

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    private final DiscoveryFeedProperties properties;

    @Override
    public FeedSession create(UUID userId) {
        UUID sessionId = UUID.randomUUID();
        String metaKey = metaKey(sessionId);
        redisTemplate.opsForHash().putAll(metaKey, Map.of("userId", userId.toString(), "exhausted", "0"));
        redisTemplate.expire(metaKey, properties.getSessionTtl());
        return new FeedSession(sessionId, userId, 0, false);
    }

    @Override
    public Optional<FeedSession> find(UUID sessionId) {
        List<?> result = redisTemplate.execute(FIND_SCRIPT, keys(sessionId), ttlMillis());
        if (result == null || result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new FeedSession(
                sessionId,
                UUID.fromString((String) result.get(0)),
                ((Number) result.get(2)).intValue(),
                "1".equals(result.get(1))));
    }

    @Override
    public int append(FeedSession session, List<FeedResponse> batch, int maxSize) {
        List<String> args = new ArrayList<>(2 + batch.size() * 2);
        args.add(String.valueOf(maxSize));
        args.add(ttlMillis());
        for (FeedResponse item : batch) {
            if (item.getPinId() != null) {
                args.add(item.getPinId().toString());
                args.add(jsonMapper.writeValueAsString(item));
            }
        }

        List<?> result = redisTemplate.execute(APPEND_SCRIPT, keys(session.getId()), args.toArray());
        session.setSize(((Number) result.get(1)).intValue());
        return ((Number) result.get(0)).intValue();
    }

    @Override
    public void markExhausted(FeedSession session) {
        redisTemplate.opsForHash().put(metaKey(session.getId()), "exhausted", "1");
        session.setExhausted();
    }

    @Override
    public List<FeedResponse> slice(UUID sessionId, int offset, int size) {
        List<String> json = redisTemplate.opsForList().range(itemsKey(sessionId), offset, offset + size - 1L);
        if (json == null) {
            return List.of();
        }
        return json.stream()
                .map(value -> jsonMapper.readValue(value, FeedResponse.class))
                .toList();
    }

    @Override
    public Set<UUID> servedPinIds(UUID sessionId) {
        Set<String> members = redisTemplate.opsForSet().members(pinsKey(sessionId));
        if (members == null) {
            return Set.of();
        }
        return members.stream().map(UUID::fromString).collect(Collectors.toUnmodifiableSet());
    }

    private String ttlMillis() {
        return String.valueOf(properties.getSessionTtl().toMillis());
    }

    private static List<String> keys(UUID sessionId) {
        return List.of(metaKey(sessionId), itemsKey(sessionId), pinsKey(sessionId));
    }

    private static String metaKey(UUID sessionId) {
        return KEY_PREFIX + sessionId + "}:meta";
    }

    private static String itemsKey(UUID sessionId) {
        return KEY_PREFIX + sessionId + "}:items";
    }

    private static String pinsKey(UUID sessionId) {
        return KEY_PREFIX + sessionId + "}:pins";
    }
}
