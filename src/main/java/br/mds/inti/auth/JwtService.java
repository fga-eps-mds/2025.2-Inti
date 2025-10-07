package br.mds.inti.auth;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Service
public class JwtService {

    private static final String secret = "arranha-ceu";
    private long expirationMillis = 1000L * 60 * 60 * 24 * 30;
    private Date expiresAt = new Date(System.currentTimeMillis() + expirationMillis);

    public String generateToken(String email) {

        return JWT.create()
                .withSubject(email)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret)).build()
                    .verify(token)
                    .getSubject();

        } catch (Exception e) {

            return null;
        }
    }

}
