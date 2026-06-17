package com.cuidared.controllers;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.models.Usuario;
import com.cuidared.services.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para la gestión de perfiles de usuario.
 */
@RestController
@RequestMapping("/api/v1/perfiles")
@CrossOrigin(origins = "*")
public class PerfilController {

    private final UsuarioService usuarioService;

    @Autowired
    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        return new ResponseEntity<>(usuarioService.guardarUsuario(usuario), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable String id) {
        Optional<Usuario> usuario = usuarioService.obtenerPorId(id);
        return usuario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- SPRINT 2 ---

    // --- Perfil propio (ligado al token de la sesión) ---
    // El id NO viene del cliente: lo pone JwtAuthFilter como atributo "usuarioId"
    // a partir del JWT validado, de modo que un usuario solo opera sobre su perfil.

    @GetMapping("/me")
    public ResponseEntity<?> obtenerMiPerfil(HttpServletRequest request) {
        String id = (String) request.getAttribute("usuarioId");
        return usuarioService.obtenerPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<?> modificarMiPerfil(HttpServletRequest request, @RequestBody Usuario cambios) {
        String id = (String) request.getAttribute("usuarioId");
        try {
            return ResponseEntity.ok(usuarioService.modificarInformacion(id, cambios));
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> eliminarMiPerfil(HttpServletRequest request) {
        String id = (String) request.getAttribute("usuarioId");
        try {
            usuarioService.eliminarPerfil(id);
            return ResponseEntity.noContent().build();
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificarInformacion(@PathVariable String id, @RequestBody Usuario cambios) {
        try {
            return ResponseEntity.ok(usuarioService.modificarInformacion(id, cambios));
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPerfil(@PathVariable String id) {
        try {
            usuarioService.eliminarPerfil(id);
            return ResponseEntity.noContent().build();
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
