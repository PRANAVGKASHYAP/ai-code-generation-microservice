package com.micro.api_gateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class jwtService {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    // write a function to validate the token
    public void validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));

        Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

}
