package com.cuidared.controllers;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.models.Notificacion;
import com.cuidared.services.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de notificaciones.
 */
@RestController
@RequestMapping("/api/v1/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Autowired
    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostMapping
    public ResponseEntity<?> registrarNotificacion(@RequestBody Notificacion notificacion) {
        try {
            Notificacion nueva = notificacionService.registrarNotificacion(notificacion);
            return new ResponseEntity<>(nueva, HttpStatus.CREATED);
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<Notificacion>> obtenerNotificaciones(
            @PathVariable String usuarioId,
            @RequestParam(name = "incluirSilenciadas", defaultValue = "false") boolean incluirSilenciadas) {
        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(usuarioId, incluirSilenciadas));
    }

    @PatchMapping("/{id}/silenciar")
    public ResponseEntity<?> silenciar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(notificacionService.silenciarNotificacion(id));
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(notificacionService.activarNotificacion(id));
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
