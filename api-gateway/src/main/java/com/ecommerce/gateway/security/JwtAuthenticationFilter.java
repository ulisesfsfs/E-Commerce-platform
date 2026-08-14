package com.ecommerce.gateway.security;

import com.ecommerce.gateway.config.GatewaySecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    private final JwtUtils jwtUtils;
    private final GatewayAuthorizationService authorizationService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        var method = exchange.getRequest().getMethod();

        if (authorizationService.isPublicPath(path, method)) {
            return chain.filter(exchange);
        }

        String token = parseJwt(exchange.getRequest());
        if (!StringUtils.hasText(token) || !jwtUtils.validateToken(token)) {
            return unauthorized(exchange);
        }

        String email = jwtUtils.getEmailFromToken(token);
        String userId = jwtUtils.getUserIdFromToken(token);
        String roles = jwtUtils.getRolesFromToken(token);

        if (!StringUtils.hasText(userId)) {
            return unauthorized(exchange);
        }

        if (authorizationService.requiresAdminRole(path, method)
                && !authorizationService.hasRole(roles, "ROLE_ADMIN")) {
            return forbidden(exchange);
        }

        if (!authorizationService.isAuthorizedForUserResource(path, userId, method)) {
            return forbidden(exchange);
        }

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(USER_EMAIL_HEADER, email)
                .header(USER_ID_HEADER, userId)
                .header(USER_ROLES_HEADER, roles)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private String parseJwt(ServerHttpRequest request) {
        String headerAuth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
