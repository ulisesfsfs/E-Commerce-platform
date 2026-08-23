package com.ecommerce.user.controller;

import com.ecommerce.user.dto.UserProfileResponse;
import com.ecommerce.user.service.UserService;
import com.ecommerce.user.domain.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(userService.getProfileByEmail(email));
    }

    @PutMapping("/profile/address")
    public ResponseEntity<?> updateAddress(@AuthenticationPrincipal String email, @RequestBody Address address) {
        userService.changeAddress(email, address);
        return ResponseEntity.ok().build();
    }
}
