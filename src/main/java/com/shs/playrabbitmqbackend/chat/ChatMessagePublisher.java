package com.shs.playrabbitmqbackend.chat;

import com.shs.playrabbitmqbackend.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishMessage(String roomId, String message) {
        // RabbitMQ에 메시지 발행
        String formattedMessage = roomId + ":" + message; // 포맷: "roomId:message"
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, formattedMessage);
    }
}