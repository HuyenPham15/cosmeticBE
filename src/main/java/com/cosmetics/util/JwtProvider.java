package com.cosmetics.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JwtProvider {

    private static final String SECRET_KEY = JwtConstant.SECRET_KEY; // Sử dụng SECRET_KEY từ JwtConstant

    public String generateToken(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        String roles = populateAuthorities(authorities);

        // Tạo JWT
        String jwt = Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 86400000)) // 1 ngày
                .claim("email", auth.getName())
                .claim("authorities", roles)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256) // Sử dụng HMAC SHA-256
                .compact();

        return jwt;
    }

    public String getEmailFromJwtToken(String jwt) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        return claims.get("email", String.class);
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Arrays.copyOf(SECRET_KEY.getBytes(StandardCharsets.UTF_8), 32); // Đảm bảo độ dài 32 bytes cho HMAC SHA-256
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> auths = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            auths.add(authority.getAuthority());
        }
        return String.join(",", auths);
    }
}