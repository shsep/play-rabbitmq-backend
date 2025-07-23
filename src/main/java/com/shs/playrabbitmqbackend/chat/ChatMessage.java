package com.shs.playrabbitmqbackend.chat;

public record ChatMessage(
    String nickname,
    String message
) {
}
