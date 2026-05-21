package com.cuidared.controllers;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.exceptions.SolapamientoHorarioException;
import com.cuidared.models.Horario;
import com.cuidared.services.AgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gestionar agenda y disponibilidad de cuidadores.
 */
@RestController
@RequestMapping("/api/v1/agenda")
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class AgendaController {

    private final AgendaService agendaService;

    @Autowired
    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @PostMapping("/disponibilidad/{cuidadorId}")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @PathVariable String cuidadorId,
            @RequestBody Horario horario
    ) {
        boolean disponible = agendaService.verificarDisponibilidadCuidador(cuidadorId, horario);
        return ResponseEntity.ok(disponible);
    }

    @PostMapping("/horarios/{cuidadorId}")
    public ResponseEntity<Horario> agregarHorarioDisponible(
            @PathVariable String cuidadorId,
            @RequestBody Horario horario
    ) {
        Horario registrado = agendaService.registrarHorarioDisponible(cuidadorId, horario);
        return new ResponseEntity<>(registrado, HttpStatus.CREATED);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<String> manejarReglaNegocio(ReglaNegocioException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(SolapamientoHorarioException.class)
    public ResponseEntity<String> manejarSolapamiento(SolapamientoHorarioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarErrorGeneral(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + ex.getMessage());
    }
}