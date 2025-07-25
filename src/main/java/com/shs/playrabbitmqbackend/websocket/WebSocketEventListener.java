package com.shs.playrabbitmqbackend.websocket;

import com.shs.playrabbitmqbackend.chat.ChatRoomManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatRoomManager chatRoomManager;

    // 세션별로 roomId와 nickname을 저장할 맵
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    // STOMP 연결 이벤트
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // STOMP headers에서 roomId와 nickname 가져오기
        String sessionId = accessor.getSessionId();
        String roomId = accessor.getFirstNativeHeader("roomId");
        String nickname = accessor.getFirstNativeHeader("nickname");

        if (roomId != null && nickname != null) {
            // 사용자 정보를 Redis에 추가
            chatRoomManager.addUserToRoom(roomId, nickname);

            // 세션 맵에 저장
            sessionRoomMap.put(sessionId, roomId);
            sessionUserMap.put(sessionId, nickname);

            log.info("User '{}' joined room '{}' with session ID: {}", nickname, roomId, sessionId);
        }
    }

    // STOMP 연결 종료 이벤트
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // 세션으로부터 정보 가져오기
        String roomId = sessionRoomMap.remove(sessionId);
        String nickname = sessionUserMap.remove(sessionId);

        log.info("Session '{}' disconnected, room {}, user {}", sessionId, roomId, nickname);

        if (roomId != null && nickname != null) {
            // Redis에서 사용자 제거
            chatRoomManager.removeUserFromRoom(roomId, nickname);
            log.info("User '{}' removed from room '{}' on session disconnect", nickname, roomId);
        } else {
            log.warn("No associated user or room found for session ID: {}", sessionId);
        }
    }
}