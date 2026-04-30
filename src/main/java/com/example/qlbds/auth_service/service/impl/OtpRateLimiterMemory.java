package com.example.qlbds.auth_service.service.impl;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.qlbds.auth_service.service.OtpRateLimiter;

@Component
public class OtpRateLimiterMemory implements OtpRateLimiter {

    private final Map<String, Instant> store = new ConcurrentHashMap<>();
    private static final long RESEND_SECONDS = 60;

    @Override
    public boolean canSend(String email) {
        Instant lastSent = store.get(email);
        if (lastSent == null) {
            return true;
        }
        return lastSent.plusSeconds(RESEND_SECONDS).isBefore(Instant.now());
    }

    @Override
    public void recordSend(String email) {
        store.put(email, Instant.now());
    }
}
