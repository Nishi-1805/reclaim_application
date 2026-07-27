package com.cdac.service;

import com.cdac.dto.request.LoginRequest;
import com.cdac.dto.request.RegisterRequest;
import com.cdac.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

}
