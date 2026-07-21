package com.swiggysystem.trackingservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SseEmitterRegistry {

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    // is pod ke andar abhi jitne SSE connections open hain, unka local map
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final String CHANNEL_PREFIX = "rider-updates:";

    public SseEmitter register(String riderId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String channel = CHANNEL_PREFIX + riderId;

        MessageListener listener = (message, pattern)
                -> pushToEmitter(emitter, message);

        redisMessageListenerContainer.addMessageListener(listener, new ChannelTopic(channel));

        Runnable cleanup = () -> {
            emitters.remove(riderId, emitter);
            redisMessageListenerContainer.removeMessageListener(listener, new ChannelTopic(channel));
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError((e) -> cleanup.run());

        emitters.put(riderId, emitter);
        return emitter;
    }

    private void pushToEmitter(SseEmitter emitter, Message message) {
        try {
            String payload = new String(message.getBody());
            emitter.send(SseEmitter.event().name("location-update").data(payload));
        } catch (Exception e) {
        }
    }
}