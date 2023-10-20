package com.example.demo.chat;

import com.example.demo.entity.User;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class ChatController {
    public static Set<User> user = new HashSet<>();
    public static Map<String, List<String>> listUser = new HashMap<>();
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        User users = new User(chatMessage.getSender(), "Online", "#32c787");
        int i = 0;
        for (User user : ChatController.user) {
            if (user.getName().equalsIgnoreCase(users.getName())) {
                user.setStatus("Online");
                user.setColor("#32c787");
                i++;
                listUser.get(user.getName()).add(headerAccessor.getSessionId());
            }
        }
        if (i == 0) {
            user.add(users);
            listUser.put(users.getName(), new ArrayList<>(List.of(headerAccessor.getSessionId())));
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
}
