package com.cdac.service.Impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.dto.request.LoginRequest;
import com.cdac.dto.request.RegisterRequest;
import com.cdac.dto.response.AuthResponse;
import com.cdac.entity.User;
import com.cdac.enums.AccountStatus;
import com.cdac.enums.UserRole;
import com.cdac.exception.DuplicateResourceException;
import com.cdac.exception.ForbiddenException;
import com.cdac.exception.ResourceNotFoundException;
import com.cdac.repository.UserRepository;
import com.cdac.security.CustomUserDetailsService;
import com.cdac.security.JwtService;
import com.cdac.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;
    
    private final CustomUserDetailsService customUserDetailsService;
    
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email is already registered.");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        String token = generateToken(savedUser);

        return buildAuthResponse(savedUser, token);
    }
    
    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(

                        request.getEmail(),

                        request.getPassword()

                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found."));
        
        if(user.getAccountStatus()!=AccountStatus.ACTIVE){
            throw new ForbiddenException(
                "Your account is not active.");
        }

        String token = generateToken(user);

        return buildAuthResponse(user, token);
    }
    
    private AuthResponse buildAuthResponse(
            User user,
            String token) {

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
    
    private String generateToken(User user) {

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(user.getEmail());

        return jwtService.generateToken(userDetails);
    }

}