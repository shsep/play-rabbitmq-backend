package com.shs.playrabbitmqbackend.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void handleMessage(String message) {
        log.info("Received message from RabbitMQ: {}", message);

        // Redis Pub/Sub 채널로 메시지 발행
        String roomId = extractRoomId(message);
        String nickname = extractNickname(message);
        String messageContent = extractMessageContent(message);
        redisTemplate.convertAndSend("chat:room:" + roomId, nickname + ": " + messageContent);

        // WebSocket 브로캐스트
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, nickname + ": " + messageContent);
    }

    private String extractRoomId(String message) {
        // 메시지 내에서 roomId 추출 (메시지 포맷에 따라 변경 필요)
        return message.split(":")[0]; // 예: "roomId:nickname:messageContent"
    }

    private String extractNickname(String message) {
        return message.split(":")[1]; // 예: "roomId:nickname:messageContent"
    }

    private String extractMessageContent(String message) {
        return message.split(":")[2]; // 예: "roomId:nickname:messageContent"
    }
}