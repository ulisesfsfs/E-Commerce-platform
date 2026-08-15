package com.ecommerce.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gateway.security")
public class GatewaySecurityProperties {

    private List<String> publicPaths = List.of(
            "/api/users/auth/**",
            "/actuator/**"
    );

    private List<String> publicGetPaths = List.of(
            "/api/products",
            "/api/products/**"
    );
}
