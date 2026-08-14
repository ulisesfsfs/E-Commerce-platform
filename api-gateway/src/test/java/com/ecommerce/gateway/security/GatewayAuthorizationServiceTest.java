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
        // Cart access - GET/PUT should verify user ownership
        assertTrue(authorizationService.isAuthorizedForUserResource("/api/carts/42", "42", HttpMethod.GET));
        assertFalse(authorizationService.isAuthorizedForUserResource("/api/carts/42", "99", HttpMethod.GET));
        
        // Create order - POST should verify userId matches authenticated user
        assertTrue(authorizationService.isAuthorizedForUserResource("/api/orders/42", "42", HttpMethod.POST));
        assertFalse(authorizationService.isAuthorizedForUserResource("/api/orders/42", "99", HttpMethod.POST));
        
        // Get order by ID - GET should NOT extract userId, should always allow (order ownership handled by service)
        assertTrue(authorizationService.isAuthorizedForUserResource("/api/orders/5", "42", HttpMethod.GET));
        
        // Get user's orders - GET should verify userId matches
        assertTrue(authorizationService.isAuthorizedForUserResource("/api/orders/user/42", "42", HttpMethod.GET));
        assertFalse(authorizationService.isAuthorizedForUserResource("/api/orders/user/42", "99", HttpMethod.GET));
    }

    @Test
    void hasRole_DetectsAdminRole() {
        assertTrue(authorizationService.hasRole("ROLE_USER,ROLE_ADMIN", "ROLE_ADMIN"));
        assertFalse(authorizationService.hasRole("ROLE_USER", "ROLE_ADMIN"));
    }
}
