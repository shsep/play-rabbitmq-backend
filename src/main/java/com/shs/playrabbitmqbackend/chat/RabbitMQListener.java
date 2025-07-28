package com.shs.playrabbitmqbackend.chat;

import com.shs.playrabbitmqbackend.bot.OllamaService;
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
    private final OllamaService ollamaService;

    public void handleMessage(String message) {
        log.info("Received message from RabbitMQ: {}", message);

        // 메세지 처리
        String roomId = extractRoomId(message);
        String nickname = extractNickname(message);
        String messageContent = extractMessageContent(message);

        // 일반 메시지
        redisTemplate.convertAndSend("chat:room:" + roomId, nickname + ": " + messageContent);
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, nickname + ": " + messageContent);

        // 메시지가 봇 호출일 경우
        if (messageContent.startsWith("!bot") || messageContent.startsWith("!봇")) {
            String botQuery = messageContent.substring(5).trim(); // "!bot " 이후의 메시지
            String botResponse = ollamaService.getBotResponse(botQuery);

            // 봇 응답 Redis 송신 및 WebSocket 브로드캐스팅
            redisTemplate.convertAndSend("chat:room:" + roomId, "Bot: " + botResponse);
            simpMessagingTemplate.convertAndSend("/topic/" + roomId, "Bot: " + botResponse);
        }
    }

    private String extractRoomId(String message) {
        return message.split(":")[0]; // 예: "roomId:nickname:messageContent"
    }

    private String extractNickname(String message) {
        return message.split(":")[1]; // 예: "roomId:nickname:messageContent"
    }

    private String extractMessageContent(String message) {
        return message.split(":")[2]; // 예: "roomId:nickname:messageContent"
    }
}