package com.cuidared.controllers;

import com.cuidared.models.Solicitud;
import com.cuidared.services.SolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.exceptions.SolapamientoHorarioException;

/**
 * Controlador REST para la gestión de intercambios (solicitudes de cuidado).
 */
@RestController
@RequestMapping("/api/v1/intercambio")
@CrossOrigin(origins = "*")
public class IntercambioController {

    private final SolicitudService solicitudService;

    @Autowired
    public IntercambioController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<?> crearSolicitud(@RequestBody Solicitud solicitud) {
        try {
            Solicitud nueva = solicitudService.crearSolicitud(solicitud);
            return new ResponseEntity<>(nueva, HttpStatus.CREATED);
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SolapamientoHorarioException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage(), "solicitudExistenteId", ""));
        }
    }

    @GetMapping("/solicitudes")
    public ResponseEntity<List<Solicitud>> listarSolicitudes() {
        return ResponseEntity.ok(solicitudService.obtenerTodas());
    }

    // --- LÓGICA DE JESÚS RECUPERADA (Historial y Calificaciones) ---
    @GetMapping("/solicitudes/padre/{padreId}")
    public ResponseEntity<Map<String, List<Solicitud>>> obtenerHistorialYFuturas(@PathVariable String padreId) {
        return ResponseEntity.ok(solicitudService.obtenerHistorialYFuturasPorPadre(padreId));
    }

    // --- SPRINT 2 ---

    @PutMapping("/solicitudes/{id}")
    public ResponseEntity<?> modificarSolicitud(@PathVariable String id, @RequestBody Solicitud cambios) {
        try {
            return ResponseEntity.ok(solicitudService.modificarSolicitud(id, cambios));
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/solicitudes/{id}/cancelar")
    public ResponseEntity<?> cancelarSolicitud(@PathVariable String id) {
        try {
            return ResponseEntity.ok(solicitudService.cancelarSolicitud(id));
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}