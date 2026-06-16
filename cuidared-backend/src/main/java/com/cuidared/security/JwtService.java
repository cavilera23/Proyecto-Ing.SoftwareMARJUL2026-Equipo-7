package com.cuidared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Genera y valida los tokens JWT que representan la sesión del usuario.
 * El token lleva el id del usuario (subject) y su rol/nombre como claims.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(String usuarioId, String tipoUsuario, String nombre) {
        Date ahora = new Date();
        return Jwts.builder()
                .setSubject(usuarioId)
                .addClaims(Map.of(
                        "tipoUsuario", tipoUsuario == null ? "" : tipoUsuario,
                        "nombre", nombre == null ? "" : nombre))
                .setIssuedAt(ahora)
                .setExpiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Devuelve los claims si el token es válido; lanza excepción si está
     * malformado, manipulado o expirado.
     */
    public Claims validarYObtenerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(clave)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
