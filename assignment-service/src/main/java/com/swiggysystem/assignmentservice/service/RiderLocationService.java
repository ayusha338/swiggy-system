package com.swiggysystem.assignmentservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiderLocationService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REDIS_GEO_KEY = "riders:locations";
    private static final String HEARTBEAT_KEY_PREFIX = "rider:heartbeat:";
    private static final String STATUS_KEY_PREFIX = "rider:status:";
    private static final String RATING_KEY_PREFIX = "rider:rating:";
    private static final String IDLE_SINCE_KEY_PREFIX = "rider:idle-since:";
    private static final Duration HEARTBEAT_TTL = Duration.ofSeconds(30);

    // Lua script ek baar load hoke cache ho jata hai, har call pe file dobara nahi padhi jaati
    private final DefaultRedisScript<Long> claimRiderScript = new DefaultRedisScript<>() {{
        setLocation(new ClassPathResource("scripts/claim_rider.lua"));
        setResultType(Long.class);
    }};

    // ---------- Location tracking ----------

    public void updateRiderLocation(String riderId, double latitude, double longitude) {
        Point point = new Point(longitude, latitude);
        redisTemplate.opsForGeo().add(REDIS_GEO_KEY, point, riderId);

        // heartbeat refresh - rider abhi "alive" hai yeh signal karta hai
        redisTemplate.opsForValue().set(HEARTBEAT_KEY_PREFIX + riderId, "alive", HEARTBEAT_TTL);
    }

    public void removeRiderLocation(String riderId) {
        redisTemplate.opsForGeo().remove(REDIS_GEO_KEY, riderId);
        redisTemplate.delete(HEARTBEAT_KEY_PREFIX + riderId);
    }

    // ---------- Availability tracking ----------

    public void setRiderAvailable(String riderId) {
        redisTemplate.opsForValue().set(STATUS_KEY_PREFIX + riderId, "AVAILABLE");
        // idle timer yahin se shuru hota hai - jab se rider available hua
        redisTemplate.opsForValue().set(IDLE_SINCE_KEY_PREFIX + riderId, String.valueOf(System.currentTimeMillis()));
    }

    public void setRiderBusy(String riderId) {
        redisTemplate.opsForValue().set(STATUS_KEY_PREFIX + riderId, "BUSY");
    }

    public boolean isRiderAvailable(String riderId) {
        String status = redisTemplate.opsForValue().get(STATUS_KEY_PREFIX + riderId);
        return "AVAILABLE".equals(status);
    }

    // ---------- Atomic claim (race-condition safe) ----------

    public boolean claimRider(String riderId) {
        Long result = redisTemplate.execute(
                claimRiderScript,
                Collections.singletonList(STATUS_KEY_PREFIX + riderId),
                "AVAILABLE",
                "BUSY"
        );
        return result != null && result == 1L;
    }

    // ---------- Rating tracking ----------

    public void setRiderRating(String riderId, double rating) {
        redisTemplate.opsForValue().set(RATING_KEY_PREFIX + riderId, String.valueOf(rating));
    }

    public double getRiderRating(String riderId) {
        String rating = redisTemplate.opsForValue().get(RATING_KEY_PREFIX + riderId);
        return rating != null ? Double.parseDouble(rating) : 3.0;   // default rating agar set nahi hai
    }

    // ---------- Idle time tracking ----------

    public long getIdleSeconds(String riderId) {
        String idleSince = redisTemplate.opsForValue().get(IDLE_SINCE_KEY_PREFIX + riderId);
        if (idleSince == null) return 0;
        long idleSinceMillis = Long.parseLong(idleSince);
        return (System.currentTimeMillis() - idleSinceMillis) / 1000;
    }

    // ---------- The actual assignment query: nearby + available + scored + sorted ----------

    public List<RiderCandidate> findBestRiders(double latitude, double longitude, double radiusKm) {
        Circle searchArea = new Circle(new Point(longitude, latitude), new Distance(radiusKm, Metrics.KILOMETERS));

        GeoResults<RedisGeoCommands.GeoLocation<String>> radius = redisTemplate.opsForGeo().radius(
                REDIS_GEO_KEY, searchArea,
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance()
        );

        return radius.getContent().stream()
                .filter(result -> {
                    String riderId = result.getContent().getName();
                    return Boolean.TRUE.equals(redisTemplate.hasKey(HEARTBEAT_KEY_PREFIX + riderId))
                            && isRiderAvailable(riderId);
                })
                .map(result -> {
                    String riderId = result.getContent().getName();
                    double distanceKm = result.getDistance().getValue();
                    double rating = getRiderRating(riderId);
                    long idleSeconds = getIdleSeconds(riderId);

                    double score = calculateScore(distanceKm, rating, idleSeconds);
                    return new RiderCandidate(riderId, distanceKm, score);
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    private double calculateScore(double distanceKm, double rating, long idleSeconds) {
        double distanceScore = 1.0 / (distanceKm + 0.1);
        double ratingScore = rating;
        double idleScore = Math.min(idleSeconds / 60.0, 5.0);

        return (distanceScore * 10) + (ratingScore * 2) + (idleScore * 1);
    }
}