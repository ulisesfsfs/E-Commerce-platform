package com.ecommerce.gateway.security;

import com.ecommerce.gateway.config.GatewaySecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.*;

class GatewayAuthorizationServiceTest {

    private GatewayAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new GatewayAuthorizationService(new GatewaySecurityProperties());
    }

    @Test
    void isPublicPath_AllowsAuthEndpoints() {
        assertTrue(authorizationService.isPublicPath("/api/users/auth/login", HttpMethod.POST));
        assertTrue(authorizationService.isPublicPath("/api/users/auth/register", HttpMethod.POST));
    }

    @Test
    void isPublicPath_AllowsProductCatalogReads() {
        assertTrue(authorizationService.isPublicPath("/api/products", HttpMethod.GET));
        assertTrue(authorizationService.isPublicPath("/api/products/abc123", HttpMethod.GET));
        assertFalse(authorizationService.isPublicPath("/api/products", HttpMethod.POST));
    }

    @Test
    void requiresAdminRole_ForProductMutations() {
        assertTrue(authorizationService.requiresAdminRole("/api/products", HttpMethod.POST));
        assertTrue(authorizationService.requiresAdminRole("/api/products/abc123", HttpMethod.PATCH));
        assertTrue(authorizationService.requiresAdminRole("/api/products/abc123", HttpMethod.DELETE));
        assertFalse(authorizationService.requiresAdminRole("/api/products/abc123/reserve", HttpMethod.POST));
    }

    @Test
    void isAuthorizedForUserResource_MatchesCartAndOrderPaths() {
        assertTrue(authorizationService.isAuthorizedForUserResource("/api/carts/42", "42"));
        assertFalse(authorizationService.isAuthorizedForUserResource("/api/carts/42", "99"));
        assertTrue(authorizationService.isAuthorizedForUserResource("/api/orders/42", "42"));
        assertTrue(authorizationService.isAuthorizedForUserResource("/api/orders/user/42", "42"));
        assertTrue(authorizationService.isAuthorizedForUserResource("/api/payments/1", "42"));
    }

    @Test
    void hasRole_DetectsAdminRole() {
        assertTrue(authorizationService.hasRole("ROLE_USER,ROLE_ADMIN", "ROLE_ADMIN"));
        assertFalse(authorizationService.hasRole("ROLE_USER", "ROLE_ADMIN"));
    }
}
