package com.cuidared.controllers;

import com.cuidared.models.Solicitud;
import com.cuidared.services.SolicitudService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<?> crearSolicitud(HttpServletRequest request, @RequestBody Solicitud solicitud) {
        // El padre que crea la solicitud se toma del token de la sesión (lo pone
        // JwtAuthFilter), no de lo que envíe el cliente: así queda asociada a quien
        // realmente está autenticado y no se puede falsear el id.
        String padreId = (String) request.getAttribute("usuarioId");
        solicitud.setPadreId(padreId);
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

    // --- LADO DEL CUIDADOR (mercado de solicitudes abiertas) ---

    // Solicitudes PENDIENTES y abiertas que el cuidador (del token) puede tomar,
    // según sus habilidades, sin importar si caen fuera de su horario declarado.
    @GetMapping("/solicitudes/disponibles")
    public ResponseEntity<?> listarDisponiblesParaCuidador(HttpServletRequest request) {
        String cuidadorId = (String) request.getAttribute("usuarioId");
        try {
            return ResponseEntity.ok(solicitudService.obtenerSolicitudesDisponiblesPara(cuidadorId));
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Solicitudes que el cuidador (del token) ya aceptó, separadas en activas e historial.
    @GetMapping("/solicitudes/cuidador/mias")
    public ResponseEntity<?> listarMisSolicitudesCuidador(HttpServletRequest request) {
        String cuidadorId = (String) request.getAttribute("usuarioId");
        try {
            return ResponseEntity.ok(solicitudService.obtenerSolicitudesDeCuidador(cuidadorId));
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // El cuidador (del token) acepta una solicitud pendiente y queda asignado a ella.
    @PatchMapping("/solicitudes/{id}/aceptar")
    public ResponseEntity<?> aceptarSolicitud(HttpServletRequest request, @PathVariable String id) {
        String cuidadorId = (String) request.getAttribute("usuarioId");
        try {
            return ResponseEntity.ok(solicitudService.aceptarSolicitud(id, cuidadorId));
        } catch (ReglaNegocioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SolapamientoHorarioException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
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