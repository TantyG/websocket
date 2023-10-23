package com.example.demo.config;

import com.example.demo.chat.ChatController;
import com.example.demo.chat.ChatMessage;
import com.example.demo.chat.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final SimpMessageSendingOperations messageTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        String sessionId = headerAccessor.getSessionId();
        log.info("Received a new web socket connection");
        messageTemplate.convertAndSend("/topic/user", ChatController.user);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            for (Map.Entry<String, List<String>> entry : ChatController.listUser.entrySet()
            ) {
                if (entry.getValue().contains(headerAccessor.getSessionId())) {
                    entry.getValue().remove(headerAccessor.getSessionId());
                    break;
                }
            }
            log.info("User disconnected: {}", username);
            if (ChatController.listUser.get(username).size() == 0) {
                ChatController.user.forEach(
                        user -> {
                            if (user.getName().equalsIgnoreCase(username)) {
                                user.setStatus("OFFLINE");
                                user.setColor("#ff5652");
                            }
                        }
                );
                var chatMessage = ChatMessage.builder()
                        .type(MessageType.LEAVE)
                        .sender(username)
                        .build();
                messageTemplate.convertAndSend("/topic/public", chatMessage);
            }
            messageTemplate.convertAndSend("/topic/user", ChatController.user);
        }
    }
}
