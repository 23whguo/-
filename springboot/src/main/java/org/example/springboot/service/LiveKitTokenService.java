package org.example.springboot.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.config.LiveKitConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class LiveKitTokenService {

    private final LiveKitConfig config;

    public LiveKitTokenService(LiveKitConfig config) {
        this.config = config;
    }

    public record LiveKitTokenResponse(String url, String token, String roomName, String identity) {}

    public LiveKitTokenResponse createToken(String requestedRoomName, String identity, String displayName) {
        if (!StringUtils.hasText(config.getUrl())) {
            throw new IllegalStateException("LiveKit URL 未配置");
        }
        if (!StringUtils.hasText(config.getApiKey()) || !StringUtils.hasText(config.getApiSecret())) {
            throw new IllegalStateException("LiveKit API Key/Secret 未配置");
        }
        if (!StringUtils.hasText(identity)) {
            throw new IllegalArgumentException("identity 不能为空");
        }

        String roomName = sanitizeRoomName(StringUtils.hasText(requestedRoomName) ? requestedRoomName : "psy-room");
        String name = StringUtils.hasText(displayName) ? displayName : identity;

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(Math.max(60, config.getTokenTtlSeconds()));

        Algorithm algorithm = Algorithm.HMAC256(config.getApiSecret());

        Map<String, Object> grants = Map.of(
                "roomJoin", true,
                "room", roomName,
                "canPublish", true,
                "canSubscribe", true,
                "canPublishData", true
        );

        String token = JWT.create()
                .withIssuer(config.getApiKey())
                .withSubject(identity)
                .withClaim("name", name)
                .withClaim("video", grants)
                .withIssuedAt(Date.from(now))
                .withNotBefore(Date.from(now.minusSeconds(5)))
                .withExpiresAt(Date.from(exp))
                .sign(algorithm);

        return new LiveKitTokenResponse(config.getUrl(), token, roomName, identity);
    }

    private static String sanitizeRoomName(String roomName) {
        String trimmed = roomName.trim();
        String safe = trimmed.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        if (safe.length() > 64) {
            safe = safe.substring(0, 64);
        }
        if (!StringUtils.hasText(safe)) {
            safe = "psy-room";
        }
        return safe;
    }
}

