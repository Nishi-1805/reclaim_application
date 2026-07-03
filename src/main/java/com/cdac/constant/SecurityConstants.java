package com.cdac.constant;

public final class SecurityConstants {

    private SecurityConstants() {
        // Prevent instantiation
    }

    public static final String AUTH_HEADER = "Authorization";

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String ROLE_ADMIN = "ADMIN";

    public static final String ROLE_USER = "USER";
}