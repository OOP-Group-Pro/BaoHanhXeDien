package com.oem.evwarranty.security;

import com.oem.evwarranty.entity.CustomUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value; // ⬅️ THÊM DÒNG NÀY

@Service
public class JwtService {
    // ⚠️ XÓA DÒNG SECRET CỐ ĐỊNH NÀY:
    // private static final String SECRET = "my_super_secret_key_123456789_my_super_secret_key_123456789";

    // 1. Thay thế bằng @Value
    @Value("${JWT_SECRET}")
    private String SECRET; // Spring sẽ injection giá trị JWT_SECRET từ application.yml/properties

    public String generateToken(UserDetails userDetails) {

        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        CustomUserDetails user = (CustomUserDetails) userDetails;

        extraClaims.put("roles", user.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList());

        extraClaims.put("centerId", user.getCenterId());

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getId().toString()) // dùng userId
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)) // 1 ngày
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

}
