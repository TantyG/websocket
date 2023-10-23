package com.example.demo.chat;

import com.example.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class ChatController {
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    public static Set<User> user = new HashSet<>();
    public static Map<String, List<String>> listUser = new HashMap<>();
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        ChatController.user.forEach(item -> {
            if (item.getName().equals(chatMessage.getSender())) {
                item.setColor("#32c787");
                item.setStatus("Online");
            }
        });
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        User users = new User(chatMessage.getSender(), "ONLINE", "#32c787");
        int i = 0;
        for (User user : ChatController.user) {
            if (user.getName().equalsIgnoreCase(users.getName())) {
                user.setStatus("ONLINE");
                user.setColor("#32c787");
                i++;
                listUser.get(user.getName()).add(headerAccessor.getSessionId());
            }
        }
        if (i == 0) {
            user.add(users);
            listUser.put(users.getName(), new ArrayList<>(List.of(Objects.requireNonNull(headerAccessor.getSessionId()))));
        }
        return chatMessage;
    }

    @GetMapping("/allUser")
    public Set<User> listUser() {
        return this.user;
    }

    @MessageMapping("/chat.listUser")
    @SendTo("/topic/user")
    public Set<User> list(
            SimpMessageHeaderAccessor headerAccessor) {
// Add username in web socket session
        return this.user;
    }

    @MessageMapping("/chat.status")
    public void changeStatus(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        ChatController.user.forEach(item -> {
            if (item.getName().equals(chatMessage.getSender()) && (ChatController.listUser.get(item.getName()).size() == 1)) {
                item.setStatus(chatMessage.getType().name());
                System.out.println(item.getStatus());
                if (item.getStatus().equalsIgnoreCase("ONLINE")) {
                    item.setColor("#32c787");
                } else {
                    item.setColor("#ffc107");
                }

            }
        });
        messagingTemplate.convertAndSend("/topic/user", ChatController.user);
    }
}
