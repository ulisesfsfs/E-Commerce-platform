package com.ecommerce.user.service;

import com.ecommerce.user.dto.AuthResponse;
import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.RegisterRequest;
import com.ecommerce.user.dto.UserProfileResponse;
import com.ecommerce.user.domain.Address;

public interface UserService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileResponse getProfile(Long id);

    UserProfileResponse getProfileByEmail(String email);

    void changeAddress(String email, Address address);
}
