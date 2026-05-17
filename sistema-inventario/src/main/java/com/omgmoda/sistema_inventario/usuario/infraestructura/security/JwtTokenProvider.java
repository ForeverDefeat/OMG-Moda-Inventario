package com.omgmoda.sistema_inventario.usuario.infraestructura.security;

import com.omgmoda.sistema_inventario.usuario.aplicacion.usecases.IJwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Implementación de IJwtTokenProvider para jjwt 0.12.6.
 *
 * Cambios respecto a 0.11.x:
 * - Jwts.parserBuilder() → Jwts.parser()
 * - .setSigningKey() → .verifyWith()
 * - .parseClaimsJws() → .parseSignedClaims()
 * - Keys.hmacShaKeyFor() sigue igual
 */
public class JwtTokenProvider implements IJwtTokenProvider {

    // Exponemos el campo para que JwtAuthenticationFilter pueda parsear el claim
    // "rol"
    final SecretKey secretKey;
    private final long expiracionHoras;

    public JwtTokenProvider(String secret, long expiracionHoras) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionHoras = expiracionHoras;
    }

    @Override
    public String generarToken(String correo, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionHoras * 3_600_000L);

        // API 0.12.x: builder fluido sin setters deprecados
        return Jwts.builder()
                .subject(correo)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String extraerCorreo(String token) {
        return parsearClaims(token).getSubject();
    }

    @Override
    public String extraerRol(String token) {
        Claims claims = parsearClaims(token);
        Object rol = claims.get("rol");
        return rol != null ? rol.toString() : null;
    }

    @Override
    public LocalDateTime obtenerExpiracion(String token) {
        Date exp = parsearClaims(token).getExpiration();
        return exp.toInstant()
                .atZone(ZoneId.of("America/Lima"))
                .toLocalDateTime();
    }

    @Override
    public boolean esValido(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Método interno ─────────────────────────────────────────────────────────

    public Claims parsearClaims(String token) {
        // API 0.12.x: Jwts.parser() en lugar de Jwts.parserBuilder()
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
