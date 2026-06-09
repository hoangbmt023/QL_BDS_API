package com.example.qlbds.conversation_service.service.impl;

import com.example.qlbds.conversation_service.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void userConnected(String username) {
        onlineUsers.add(username);
        messagingTemplate.convertAndSend("/topic/presence", new PresenceEvent(username, true));
    }

    public void userDisconnected(String username) {
        onlineUsers.remove(username);
        messagingTemplate.convertAndSend("/topic/presence", new PresenceEvent(username, false));
    }

    public boolean isUserOnline(String username) {
        return onlineUsers.contains(username);
    }
    
}
