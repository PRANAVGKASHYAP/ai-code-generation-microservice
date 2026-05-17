package com.micro.common_lib.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private AuthUtil jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String header = request.getHeader("Authorization"); // this needs to have the Bearer: token
        if(header == null || !header.startsWith("Bearer")){
            filterChain.doFilter(request,response);
            return;
        }

        String token = header.split("Bearer ")[1];

        //decode the token
        JwtUserContext context  = jwtUtils.getIdFromToken(token);
        if(context != null && SecurityContextHolder.getContext().getAuthentication() == null){
            //make a new authentication object for this user
            Authentication auth = new UsernamePasswordAuthenticationToken(context , token , context.authorities());
            SecurityContextHolder.getContext().setAuthentication(auth); // this setAuthentication will make the Authentication object avaliable throught the microservices as is it shared by the common-lib
        }

        filterChain.doFilter(request,response);

    }
}
