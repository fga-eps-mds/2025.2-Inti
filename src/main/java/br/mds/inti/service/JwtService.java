package br.mds.inti.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.mds.inti.model.entity.Profile;

@Service
public class JwtService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(Profile profile) {
        String email = profile.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Profile email must be defined to generate a JWT token");
        }

        long expirationMillis = 1000L * 60 * 60 * 24 * 30;
        Date expiresAt = new Date(System.currentTimeMillis() + expirationMillis);

        return JWT.create()
                .withSubject(email)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateToken(String token) {
        return extractEmail(token);
    }

    private String extractEmail(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token inválido ou expirado");
        }
    }

}
