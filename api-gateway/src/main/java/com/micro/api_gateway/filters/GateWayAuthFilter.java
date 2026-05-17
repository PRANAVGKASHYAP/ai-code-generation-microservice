package com.micro.api_gateway.filters;

import com.micro.api_gateway.config.SecurityProperties;
import com.micro.api_gateway.service.jwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
@Component
@RequiredArgsConstructor
public class GateWayAuthFilter implements GlobalFilter, Ordered {

    private final SecurityProperties securityProperties;
    private final AntPathMatcher antPathMatcher;
    private final jwtService jwtService;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String requestedRoute = request.getURI().getPath();
        Boolean isPublic = securityProperties.getPublicRoutes().stream().anyMatch(route -> antPathMatcher.match(route, requestedRoute));

        if (isPublic) {
            return chain.filter(exchange);
        }

        String currHeader = request.getHeaders().getFirst("Authorization");
        if ( currHeader == null || !currHeader.startsWith("Bearer")){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // if the code reaches here then the request has the correct header , now extract the token and then parse the jwt token
        String token = currHeader.substring(7);
        // pares the jwt token here
        try {
            jwtService.validateToken(token);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // this oder is where the filter is applied in the chain , lower the order , higher the priority
        return -1; // the value is from -INF TO INF
    }
}
