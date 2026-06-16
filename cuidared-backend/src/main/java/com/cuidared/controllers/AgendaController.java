package com.cuidared.controllers;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.exceptions.SolapamientoHorarioException;
import com.cuidared.models.Cuidador;
import com.cuidared.models.Horario;
import com.cuidared.services.AgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller para gestionar agenda y disponibilidad de cuidadores.
 */
@RestController
@RequestMapping("/api/v1/agenda")
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
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

    @PostMapping("/buscar-cuidadores")
    public ResponseEntity<List<Cuidador>> buscarCuidadores(@RequestBody Horario horario) {
        List<Cuidador> disponibles = agendaService.buscarCuidadoresDisponibles(horario);
        return ResponseEntity.ok(disponibles);
    }

    @GetMapping("/horarios/{cuidadorId}")
    public ResponseEntity<List<Horario>> obtenerHorarios(@PathVariable String cuidadorId) {
        List<Horario> horarios = agendaService.obtenerHorariosCuidador(cuidadorId);
        return ResponseEntity.ok(horarios);
    }

    @PostMapping("/horarios/{cuidadorId}")
    public ResponseEntity<Horario> agregarHorarioDisponible(
            @PathVariable String cuidadorId,
            @RequestBody Horario horario
    ) {
        Horario registrado = agendaService.registrarHorarioDisponible(cuidadorId, horario);
        return new ResponseEntity<>(registrado, HttpStatus.CREATED);
    }

    // --- SPRINT 2 ---

    @PutMapping("/horarios/{cuidadorId}/{indice}")
    public ResponseEntity<Horario> modificarHorario(
            @PathVariable String cuidadorId,
            @PathVariable int indice,
            @RequestBody Horario horario
    ) {
        Horario actualizado = agendaService.modificarHorario(cuidadorId, indice, horario);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/horarios/{cuidadorId}/{indice}")
    public ResponseEntity<Void> eliminarHorario(
            @PathVariable String cuidadorId,
            @PathVariable int indice
    ) {
        agendaService.eliminarHorario(cuidadorId, indice);
        return ResponseEntity.noContent().build();
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
        // El detalle técnico queda en los logs del servidor, NUNCA se le muestra al usuario.
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocurrió un error inesperado. Inténtalo de nuevo en unos momentos.");
    }
}