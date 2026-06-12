package com.example.qlbds.auth_service.repository.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.qlbds.auth_service.model.Otp;
import com.example.qlbds.auth_service.repository.OtpRepository;

@Component
public class OtpMemoryStore implements OtpRepository {
    private final Map<String, Otp> store = new ConcurrentHashMap<>();

    @Override
    public void save(String email, Otp otp) {
        store.put(email, otp);
    }

    @Override
    public Otp get(String email) {
        return store.get(email);
    }

    @Override
    public void remove(String email) {
        store.remove(email);
    }
}
