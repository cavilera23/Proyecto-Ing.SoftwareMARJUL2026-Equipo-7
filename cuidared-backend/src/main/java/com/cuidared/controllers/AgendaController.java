package com.cuidared.controllers;

import com.cuidared.models.Horario;
import com.cuidared.services.AgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de la agenda y disponibilidad.
 */
@RestController
@RequestMapping("/api/v1/agenda")
@CrossOrigin(origins = "*") // Permite peticiones desde el frontend Vue
public class AgendaController {

    private final AgendaService agendaService;

    @Autowired
    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @PostMapping("/disponibilidad/{cuidadorId}")
    public ResponseEntity<Boolean> verificarDisponibilidad(@PathVariable String cuidadorId, @RequestBody Horario horario) {
        boolean disponible = agendaService.verificarDisponibilidadCuidador(cuidadorId, horario);
        return ResponseEntity.ok(disponible);
    }
}
