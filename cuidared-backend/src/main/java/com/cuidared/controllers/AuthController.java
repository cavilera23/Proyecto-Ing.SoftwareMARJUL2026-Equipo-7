package com.cuidared.controllers;

import com.cuidared.models.Usuario;
import com.cuidared.repositories.UsuarioRepository;
import com.cuidared.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Autenticación: inicio de sesión y emisión del token JWT.
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    /**
     * Valida correo + contraseña y, si son correctos, devuelve el token y el
     * usuario (con su tipoUsuario, que el frontend usa para dirigir al dashboard).
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String correo = credenciales.get("correo");
        String contrasena = credenciales.get("contrasena");

        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El correo y la contraseña son obligatorios."));
        }

        Optional<Usuario> encontrado = usuarioRepository.findByCorreo(correo.trim());

        // Mismo mensaje para "no existe" y "clave incorrecta": no revelar cuál falló.
        if (encontrado.isEmpty() || !contrasena.equals(encontrado.get().getContrasena())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Correo o contraseña incorrectos."));
        }

        Usuario usuario = encontrado.get();
        String token = jwtService.generarToken(
                usuario.getId(), usuario.getTipoUsuario(), usuario.getNombre());

        return ResponseEntity.ok(Map.of("token", token, "usuario", usuario));
    }
}
