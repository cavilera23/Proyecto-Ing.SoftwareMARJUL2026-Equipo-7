package com.cuidared.controllers;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.models.Calificacion;
import com.cuidared.services.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de calificaciones.
 */
@RestController
@RequestMapping("/api/v1/calificaciones")
@CrossOrigin(origins = "*")
public class CalificacionController {

    private final CalificacionService calificacionService;

    @Autowired
    public CalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    @PostMapping
    public ResponseEntity<?> crearCalificacion(@RequestBody Calificacion calificacion) {
        try {
            Calificacion nueva = calificacionService.crearCalificacion(calificacion);
            return new ResponseEntity<>(nueva, HttpStatus.CREATED);
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Calificacion>> listarCalificaciones() {
        return ResponseEntity.ok(calificacionService.obtenerTodas());
    }

    @GetMapping("/cuidador/{cuidadorId}")
    public ResponseEntity<List<Calificacion>> listarPorCuidador(@PathVariable String cuidadorId) {
        return ResponseEntity.ok(calificacionService.obtenerPorCuidador(cuidadorId));
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<Calificacion> obtenerPorSolicitud(@PathVariable String solicitudId) {
        return calificacionService.obtenerPorSolicitud(solicitudId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- SPRINT 2 ---

    @PutMapping("/{id}")
    public ResponseEntity<?> modificarCalificacion(@PathVariable String id, @RequestBody Calificacion cambios) {
        try {
            return ResponseEntity.ok(calificacionService.modificarCalificacion(id, cambios));
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCalificacion(@PathVariable String id) {
        try {
            calificacionService.eliminarCalificacion(id);
            return ResponseEntity.noContent().build();
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
