package com.cuidared.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Controlador REST para la gestión de notificaciones.
 * (Esqueleto básico para Sprint 1)
 */
@RestController
@RequestMapping("/api/v1/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<String>> obtenerNotificaciones(@PathVariable String usuarioId) {
        // En Sprint 1: Simulación de obtener notificaciones
        return ResponseEntity.ok(Collections.singletonList("Bienvenido a CuidaRed, " + usuarioId));
    }
}
