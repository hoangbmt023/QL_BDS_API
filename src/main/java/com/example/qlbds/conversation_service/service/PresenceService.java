package com.example.qlbds.conversation_service.service;

public interface PresenceService {

    void userConnected(String username);

    void userDisconnected(String username);

    boolean isUserOnline(String username);

    record PresenceEvent(String username, boolean online) {}
}
