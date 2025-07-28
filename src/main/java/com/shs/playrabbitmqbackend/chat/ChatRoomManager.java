package com.shs.playrabbitmqbackend.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomManager {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatMessagePublisher chatMessagePublisher;

    @PostConstruct
    public void initialize() {
        log.info("Initializing chat rooms - Clearing all users");

        // Redis에서 저장된 모든 채팅방 ID 가져오기
        Set<String> chatRooms = redisTemplate.opsForSet().members("chat:rooms");

        if (chatRooms != null) {
            for (String roomId : chatRooms) {
                String userSetKey = "chat:room:" + roomId + ":users";

                // 각 채팅방의 사용자 목록 초기화
                redisTemplate.delete(userSetKey);
                log.info("Cleared users for chat room: {}", roomId);
            }
        }

        log.info("All chat rooms are initialized and cleared of users.");
    }

    public String createChatRoom(String creator, String title) {
        // 방 ID 생성
        String roomId = UUID.randomUUID().toString().replace("-", "");
        ChatRoom chatRoom = new ChatRoom(roomId, creator, title, LocalDateTime.now());

        try {
            // 채팅방 세부 정보 저장
            String chatRoomJson = objectMapper.writeValueAsString(chatRoom);
            redisTemplate.opsForHash().put("chat:room:details", roomId, chatRoomJson);

            // 채팅방 ID 목록 저장
            redisTemplate.opsForSet().add("chat:rooms", roomId);
        } catch (JsonProcessingException e) {
            log.error("Error while creating chat room", e);
        }

        return roomId;
    }

    public List<ChatRoom> getAllChatRoomDetails() {
        // Redis에 저장된 모든 방 상세 정보를 가져오기
        Map<Object, Object> chatRoomsMap = redisTemplate.opsForHash().entries("chat:room:details");
        return chatRoomsMap.values().stream()
            .map(value -> {
                try {
                    return objectMapper.readValue(value.toString(), ChatRoom.class);
                } catch (JsonProcessingException e) {
                    log.error("Error while parsing chat room details", e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public void addUserToRoom(String roomId, String nickname) {
        // 이미 사용자가 채팅방에 있는지 확인
        Boolean isMember = redisTemplate.opsForSet().isMember("chat:room:" + roomId + ":users", nickname);

        if (Boolean.TRUE.equals(isMember)) {
            throw new IllegalArgumentException("User " + nickname + " is already in the room " + roomId);
        }

        redisTemplate.opsForSet().add("chat:room:" + roomId + ":users", nickname);

        chatMessagePublisher.publishMessage(roomId, new ChatMessage("SYSTEM", nickname + " has joined the room."));
    }

    public void removeUserFromRoom(String roomId, String nickname) {
        redisTemplate.opsForSet().remove("chat:room:" + roomId + ":users", nickname);

        chatMessagePublisher.publishMessage(roomId, new ChatMessage("SYSTEM", nickname + " has left the room."));
    }

    public Set<String> getUsersInRoom(String roomId) {
        return redisTemplate.opsForSet().members("chat:room:" + roomId + ":users");
    }

}
