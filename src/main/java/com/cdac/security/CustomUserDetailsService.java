package com.cdac.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cdac.entity.User;
import com.cdac.enums.AccountStatus;
import com.cdac.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
        		.orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(
                        Collections.singletonList(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name())))
                .accountLocked(user.getAccountStatus() == AccountStatus.DEACTIVATED)
                .disabled(user.getAccountStatus() == AccountStatus.INACTIVE)
                .build();
    }
}