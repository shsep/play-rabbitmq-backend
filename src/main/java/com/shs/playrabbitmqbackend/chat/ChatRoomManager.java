package com.shs.playrabbitmqbackend.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomManager {

    private final StringRedisTemplate redisTemplate;

    public void createChatRoom(String roomId) {
        // 채팅방 리스트에 저장
        redisTemplate.opsForSet().add("chat:rooms", roomId);
    }

    public Set<String> getChatRooms() {
        // 모든 채팅방 ID 가져오기
        return redisTemplate.opsForSet().members("chat:rooms");
    }

    public void addUserToRoom(String roomId, String userId) {
        // 사용자를 특정 채팅방에 추가
        redisTemplate.opsForSet().add("chat:room:" + roomId + ":users", userId);
    }

    public Set<String> getUsersInRoom(String roomId) {
        // 특정 채팅방 내 사용자 목록 반환
        return redisTemplate.opsForSet().members("chat:room:" + roomId + ":users");
    }

}
