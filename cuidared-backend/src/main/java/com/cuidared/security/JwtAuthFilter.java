package com.cuidared.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Exige un token JWT válido en el header Authorization para todas las rutas
 * /api/v1/** salvo el login y el registro de perfil (que son públicos).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // El navegador manda OPTIONS (preflight CORS) antes del request real: dejarlo pasar.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || esRutaPublica(request)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            rechazar(response);
            return;
        }

        try {
            Claims claims = jwtService.validarYObtenerClaims(header.substring(7));
            // Disponibles por si algún controller necesita saber quién hace la petición.
            request.setAttribute("usuarioId", claims.getSubject());
            request.setAttribute("tipoUsuario", claims.get("tipoUsuario"));
        } catch (Exception e) {
            rechazar(response);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean esRutaPublica(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/")) return true;                 // recursos no-API
        if (path.startsWith("/api/v1/auth/")) return true;             // login
        // Registro de un nuevo perfil (POST /perfiles) es público.
        return "POST".equalsIgnoreCase(request.getMethod()) && path.equals("/api/v1/perfiles");
    }

    private void rechazar(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Header CORS para que el navegador muestre el 401 y no un error de CORS.
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"No autenticado. Inicia sesión.\"}");
    }
}
