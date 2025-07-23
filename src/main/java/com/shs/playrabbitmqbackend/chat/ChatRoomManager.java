package com.shs.playrabbitmqbackend.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public Set<String> getChatRooms() {
        return redisTemplate.opsForSet().members("chat:rooms");
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

    public void addUserToRoom(String roomId, String userId) {
        redisTemplate.opsForSet().add("chat:room:" + roomId + ":users", userId);
    }

    public Set<String> getUsersInRoom(String roomId) {
        return redisTemplate.opsForSet().members("chat:room:" + roomId + ":users");
    }

}
