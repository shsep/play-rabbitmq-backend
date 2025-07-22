package com.shs.playrabbitmqbackend.controller;

import com.shs.playrabbitmqbackend.chat.ChatMessagePublisher;
import com.shs.playrabbitmqbackend.chat.ChatRoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatRoomController {

    private final ChatRoomManager chatRoomManager;
    private final ChatMessagePublisher chatMessagePublisher;

    @PostMapping
    public ResponseEntity<String> createChatRoom(@RequestParam String roomId) {
        chatRoomManager.createChatRoom(roomId);
        return ResponseEntity.ok("Chat room created: " + roomId);
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<String> joinChatRoom(@PathVariable String roomId, @RequestParam String userId) {
        chatRoomManager.addUserToRoom(roomId, userId);
        return ResponseEntity.ok("User " + userId + " joined room " + roomId);
    }

    @PostMapping("/{roomId}/message")
    public ResponseEntity<String> sendMessage(@PathVariable String roomId, @RequestBody String message) {
        log.info("Sending message to room {}: {}", roomId, message);
        chatMessagePublisher.publishMessage(roomId, message);
        return ResponseEntity.ok("Message sent to room " + roomId);
    }

    @GetMapping
    public ResponseEntity<Set<String>> getChatRooms() {
        return ResponseEntity.ok(chatRoomManager.getChatRooms());
    }
}
