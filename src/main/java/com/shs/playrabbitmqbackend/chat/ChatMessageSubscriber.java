package com.shs.playrabbitmqbackend.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        String content = new String(message.getBody());
        log.info("Message received from channel {}: {}", channel, content);

        // WebSocket 클라이언트로 메시지 전달
        String roomId = channel.split(":")[2]; // 예: "chat:room:<roomId>"
        messagingTemplate.convertAndSend("/topic/" + roomId, content);
    }

}
