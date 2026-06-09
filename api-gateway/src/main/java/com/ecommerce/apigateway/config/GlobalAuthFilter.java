package com.ecommerce.apigateway.config;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public GlobalAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private final List<String> openEndpoints = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/eureka"
    );

    private final Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        
        // Open endpoints bypass
        if (openEndpoints.stream().anyMatch(path::contains)) {
            return false;
        }

        // GET products and categories are open
        if (path.equals("/api/v1/products") || path.startsWith("/api/v1/products/")) {
            return !request.getMethod().name().equals("GET");
        }
        if (path.equals("/api/v1/categories") || path.startsWith("/api/v1/categories/")) {
            return !request.getMethod().name().equals("GET");
        }

        return true;
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isSecured.test(request)) {
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            if (jwtUtil.isExpired(token)) {
                return onError(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
            }

            try {
                Claims claims = jwtUtil.getClaims(token);
                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                String role = claims.get("role", String.class);

                // Add claims to request headers for downstream microservices
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "Invalid token claims", HttpStatus.UNAUTHORIZED);
            }
        }
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Execute early in the gateway chain
    }
}
