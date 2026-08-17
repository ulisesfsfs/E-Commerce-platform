package com.ecommerce.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/cart-service")
    public Mono<ResponseEntity<Map<String, String>>> cartServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Service Unavailable",
                        "message", "Cart service is temporarily unavailable. Please try again later.",
                        "status", "503"
                )));
    }

    @RequestMapping("/default")
    public Mono<ResponseEntity<Map<String, String>>> defaultFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Service Unavailable",
                        "message", "The requested service is currently experiencing issues. Please try again shortly.",
                        "status", "503"
                )));
    }
}
