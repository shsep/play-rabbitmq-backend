package com.shs.playrabbitmqbackend.controller;

import com.shs.playrabbitmqbackend.chat.ChatMessage;
import com.shs.playrabbitmqbackend.chat.ChatMessagePublisher;
import com.shs.playrabbitmqbackend.chat.ChatRoom;
import com.shs.playrabbitmqbackend.chat.ChatRoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatRoomController {

    private final ChatRoomManager chatRoomManager;
    private final ChatMessagePublisher chatMessagePublisher;

    /**
     * 채팅방 생성
     * @param creator 방 생성자 닉네임
     * @param title 방 제목
     * @return 생성된 방의 ID
     */
    @PostMapping
    public ResponseEntity<String> createChatRoom(@RequestParam String creator, @RequestParam String title) {
        String roomId = chatRoomManager.createChatRoom(creator, title);
        return ResponseEntity.ok("Chat room created: " + roomId);
    }

    /**
     * 모든 채팅방 가져오기
     * @return 모든 방의 상세 정보
     */
    @GetMapping
    public ResponseEntity<List<ChatRoom>> getChatRooms() {
        return ResponseEntity.ok(chatRoomManager.getAllChatRoomDetails());
    }

    /**
     * 사용자를 채팅방에 추가
     * @param roomId 채팅방 ID
     * @param nickname 추가할 사용자 닉네임
     * @return 성공 메시지
     */
    @PostMapping("/{roomId}/join")
    public ResponseEntity<String> joinChatRoom(@PathVariable String roomId, @RequestParam String nickname) {
        chatRoomManager.addUserToRoom(roomId, nickname);
        return ResponseEntity.ok("User " + nickname + " joined room " + roomId);
    }

    /**
     * 사용자를 채팅방에서 제거
     * @param roomId 채팅방 ID
     * @param nickname 제거할 사용자 닉네임
     * @return 성공 메시지
     */
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<String> leaveChatRoom(@PathVariable String roomId, @RequestParam String nickname) {
        chatRoomManager.removeUserFromRoom(roomId, nickname);
        return ResponseEntity.ok("User " + nickname + " left room " + roomId);
    }

    @PostMapping("/{roomId}/message")
    public ResponseEntity<String> sendMessage(@PathVariable String roomId, @RequestBody ChatMessage chatMessage) {
        log.info("Sending message to room {}: {}", roomId, chatMessage);
        chatMessagePublisher.publishMessage(roomId, chatMessage);
        return ResponseEntity.ok("Message sent to room " + roomId);
    }

}