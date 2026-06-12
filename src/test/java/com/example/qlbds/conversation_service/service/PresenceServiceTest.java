package com.example.qlbds.conversation_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.qlbds.conversation_service.service.impl.PresenceServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PresenceServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private PresenceServiceImpl presenceService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("userConnected_Success")
    void userConnected_Success() {
        String username = "testuser";
        presenceService.userConnected(username);

        assertTrue(presenceService.isUserOnline(username));
        verify(messagingTemplate).convertAndSend(eq("/topic/presence"), any(PresenceService.PresenceEvent.class));
    }

    @Test
    @DisplayName("userConnected_MultipleUsers")
    void userConnected_MultipleUsers() {
        presenceService.userConnected("user1");
        presenceService.userConnected("user2");

        assertTrue(presenceService.isUserOnline("user1"));
        assertTrue(presenceService.isUserOnline("user2"));
    }

    @Test
    @DisplayName("userDisconnected_Success")
    void userDisconnected_Success() {
        String username = "testuser";
        presenceService.userConnected(username);
        assertTrue(presenceService.isUserOnline(username));

        presenceService.userDisconnected(username);

        assertFalse(presenceService.isUserOnline(username));
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/presence"), any(PresenceService.PresenceEvent.class));
    }

    @Test
    @DisplayName("userDisconnected_UserNotExists")
    void userDisconnected_UserNotExists() {
        String username = "testuser";
        // Do not connect the user first
        presenceService.userDisconnected(username);

        assertFalse(presenceService.isUserOnline(username));
    }

    @Test
    @DisplayName("isUserOnline_True")
    void isUserOnline_True() {
        String username = "testuser";
        presenceService.userConnected(username);

        assertTrue(presenceService.isUserOnline(username));
    }

    @Test
    @DisplayName("isUserOnline_False")
    void isUserOnline_False() {
        String username = "testuser";

        assertFalse(presenceService.isUserOnline(username));
    }
}
