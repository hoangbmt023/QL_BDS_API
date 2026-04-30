package com.example.qlbds.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Service xử lý JWT: tạo token, trích xuất claims, kiểm tra hợp lệ.
 * Sử dụng JJWT 0.12.x API (không dùng các method deprecated từ 0.12.0).
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ==================== Public API ====================

    /**
     * Lấy username (subject) từ token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Lấy một claim bất kỳ từ token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Tạo token cho UserDetails (thêm extra claims mặc định là email và role)
     */
    public String generateToken(UserDetails userDetails, String email, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", email);
        extraClaims.put("role", role);
        return generateToken(extraClaims, userDetails);
    }

    /**
     * Tạo token với extra claims tùy chỉnh.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Kiểm tra token hợp lệ: đúng username và chưa hết hạn.
     */
    public boolean isValidToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ==================== Private helpers ====================

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)                             // JJWT 0.12.x: thay setClaims()
                .subject(userDetails.getUsername())              // JJWT 0.12.x: thay setSubject()
                .issuedAt(new Date(now))                         // JJWT 0.12.x: thay setIssuedAt()
                .expiration(new Date(now + expiration))          // JJWT 0.12.x: thay setExpiration()
                .signWith(getSigningKey())                        // JJWT 0.12.x: tự suy ra thuật toán từ key
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())                     // JJWT 0.12.x: thay setSigningKey()
                .build()
                .parseSignedClaims(token)                        // JJWT 0.12.x: thay parseClaimsJws()
                .getPayload();                                   // JJWT 0.12.x: thay getBody()
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
