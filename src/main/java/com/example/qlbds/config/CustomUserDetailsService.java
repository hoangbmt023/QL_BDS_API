package com.example.qlbds.config;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// custom UserDetailsService để load user từ DB và kiểm tra trạng thái tài khoản
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found or deleted: " + username));

        // Kiểm tra tài khoản có bị vô hiệu hóa không
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("Account is disabled: " + username);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}
