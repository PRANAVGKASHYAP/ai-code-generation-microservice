package com.micro.common_lib.security;


import com.micro.common_lib.DTO.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

@Component
public class AuthUtil {

    // this is a util class for handelling jwt tokens
    @Value("${jwt.secret-key}")
    private String jwtScetet;


    public SecretKey signKey(){
        return Keys.hmacShaKeyFor(jwtScetet.getBytes(StandardCharsets.UTF_8));
    }


    public String generateAccessToken(JwtUserContext user){
        //ust the jwts builder

        return Jwts.builder()
                .subject(user.userName())
                .claim("userId" , user.userId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000*60*10))
                .signWith(signKey())
                .compact();
    }


    public JwtUserContext getIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.valueOf(claims.get("userId" , String.class));
        String userName = claims.getSubject();

        return new JwtUserContext(userId , userName , new ArrayList<>() , null);
    }

    //helper function to get user id from a request
    public Long getCurrentUserId(){
        Authentication currAuth = SecurityContextHolder.getContext().getAuthentication();

        if(currAuth == null || !(currAuth.getPrincipal() instanceof JwtUserContext)){
            throw new AuthenticationCredentialsNotFoundException("No jwt token found in this request");
        }
        return ((JwtUserContext) currAuth.getPrincipal()).userId();
    }
}
