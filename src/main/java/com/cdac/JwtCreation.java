package com.cdac;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Encoders;

import javax.crypto.SecretKey;

public class JwtCreation {

    public static void main(String[] args) {

        @SuppressWarnings("deprecation")
		SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

        String secret = Encoders.BASE64.encode(key.getEncoded());

        System.out.println(secret);
    }
}