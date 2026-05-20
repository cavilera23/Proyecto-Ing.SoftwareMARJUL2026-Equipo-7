package com.cuidared.controllers;

import com.cuidared.models.Solicitud;
import com.cuidared.services.SolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Solicitud> crearSolicitud(@RequestBody Solicitud solicitud) {
        Solicitud nueva = solicitudService.crearSolicitud(solicitud);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @GetMapping("/solicitudes")
    public ResponseEntity<List<Solicitud>> listarSolicitudes() {
        return ResponseEntity.ok(solicitudService.obtenerTodas());
    }
}
