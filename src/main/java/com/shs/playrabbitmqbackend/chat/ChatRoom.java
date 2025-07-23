package com.shs.playrabbitmqbackend.chat;

import java.time.LocalDateTime;

public record ChatRoom(
    String roomId,
    String creator,
    String title,
    LocalDateTime createdAt
) {

}
