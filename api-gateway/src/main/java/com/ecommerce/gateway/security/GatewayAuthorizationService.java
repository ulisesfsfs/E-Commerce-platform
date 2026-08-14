package com.ecommerce.gateway.security;

import com.ecommerce.gateway.config.GatewaySecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GatewayAuthorizationService {

    private static final Pattern CART_USER_PATTERN = Pattern.compile("^/api/carts/([^/]+).*");
    private static final Pattern ORDER_CREATE_PATTERN = Pattern.compile("^/api/orders/([^/]+)$");
    private static final Pattern ORDER_USER_LIST_PATTERN = Pattern.compile("^/api/orders/user/([^/]+).*");

    private final GatewaySecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean isPublicPath(String path, HttpMethod method) {
        if (matchesAny(path, securityProperties.getPublicPaths())) {
            return true;
        }
        return HttpMethod.GET.equals(method) && matchesAny(path, securityProperties.getPublicGetPaths());
    }

    public boolean requiresAdminRole(String path, HttpMethod method) {
        if (!path.startsWith("/api/products") || HttpMethod.GET.equals(method)) {
            return false;
        }

        if (HttpMethod.POST.equals(method) && "/api/products".equals(path)) {
            return true;
        }

        if (HttpMethod.PATCH.equals(method) || HttpMethod.DELETE.equals(method)) {
            return path.matches("^/api/products/[^/]+$");
        }

        return false;
    }

    public boolean hasRole(String rolesClaim, String requiredRole) {
        if (rolesClaim == null || rolesClaim.isBlank()) {
            return false;
        }
        return Arrays.stream(rolesClaim.split(","))
                .map(String::trim)
                .anyMatch(role -> role.equals(requiredRole));
    }

    public boolean isAuthorizedForUserResource(String path, String authenticatedUserId, HttpMethod method) {
        String pathUserId = extractUserIdFromPath(path, method);
        return pathUserId == null || pathUserId.equals(authenticatedUserId);
    }

    private String extractUserIdFromPath(String path, HttpMethod method) {
        Matcher cartMatcher = CART_USER_PATTERN.matcher(path);
        if (cartMatcher.matches()) {
            return cartMatcher.group(1);
        }

        // ORDER_CREATE_PATTERN should only apply to POST requests (creating an order with userId)
        // GET requests to /api/orders/{orderId} should not extract userId
        if (HttpMethod.POST.equals(method)) {
            Matcher orderCreateMatcher = ORDER_CREATE_PATTERN.matcher(path);
            if (orderCreateMatcher.matches()) {
                return orderCreateMatcher.group(1);
            }
        }

        Matcher orderUserListMatcher = ORDER_USER_LIST_PATTERN.matcher(path);
        if (orderUserListMatcher.matches()) {
            return orderUserListMatcher.group(1);
        }

        return null;
    }

    private boolean matchesAny(String path, List<String> patterns) {
        return patterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
