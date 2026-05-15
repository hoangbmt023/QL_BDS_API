package com.example.qlbds.auth_service.repository;

import com.example.qlbds.auth_service.model.Otp;

public interface OtpRepository {
    void save(String email, Otp otp);
    Otp get(String email);
    void remove(String email);
}
