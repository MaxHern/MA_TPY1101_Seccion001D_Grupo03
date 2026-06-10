package com.vecizervi.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Duración del token: 24 horas
    private static final long EXPIRACION_MS = 86_400_000L;

    // Clave secreta — en producción muévela a application.properties o variable de entorno
    private final SecretKey clave = Keys.hmacShaKeyFor(
        "vecizervi-clave-secreta-minimo32chars!!".getBytes()
    );

    public String generarToken(Long idUsuario, String rol) {
        return Jwts.builder()
            .subject(String.valueOf(idUsuario))
            .claim("rol", rol)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
            .signWith(clave)
            .compact();
    }

    public Long extraerIdUsuario(String token) {
        return Long.parseLong(
            Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject()
        );
    }

    public boolean esValido(String token) {
        try {
            extraerIdUsuario(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}