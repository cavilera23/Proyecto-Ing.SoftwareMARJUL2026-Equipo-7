package com.cuidared.services;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.exceptions.SolapamientoHorarioException;
import com.cuidared.models.Cuidador;
import com.cuidared.models.Horario;
import com.cuidared.models.Solicitud;
import com.cuidared.models.Usuario;
import com.cuidared.repositories.SolicitudRepository;
import com.cuidared.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la agenda, horarios y solapamientos.
 */
@Service
public class AgendaService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public AgendaService(SolicitudRepository solicitudRepository, UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Horario registrarHorarioDisponible(String cuidadorId, Horario nuevoHorario) {

        if (cuidadorId == null || cuidadorId.isBlank()) {
            throw new ReglaNegocioException("El ID del cuidador es obligatorio.");
        }

        if (nuevoHorario == null) {
            throw new ReglaNegocioException("El horario es obligatorio.");
        }

        if (nuevoHorario.getFechaInicio() == null || nuevoHorario.getFechaFin() == null) {
            throw new ReglaNegocioException("La fecha de inicio y fin son obligatorias.");
        }

        if (!nuevoHorario.getFechaInicio().isBefore(nuevoHorario.getFechaFin())) {
            throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        Optional<Usuario> userOpt = usuarioRepository.findById(cuidadorId);

        if (userOpt.isEmpty()) {
            throw new ReglaNegocioException("El usuario con ID suministrado no existe.");
        }

        Usuario usuario = userOpt.get();

        if (!(usuario instanceof Cuidador)) {
            throw new ReglaNegocioException(
                    "El usuario existe, pero no fue cargado como Cuidador. Revisa que en usuarios.json tenga \"tipoUsuario\": \"CUIDADOR\"."
            );
        }

        Cuidador cuidador = (Cuidador) usuario;

        for (Horario horarioExistente : cuidador.getHorariosDisponibles()) {
            if (horarioExistente.seSolapaCon(nuevoHorario)) {
                throw new SolapamientoHorarioException(
                        "El horario seleccionado choca con un bloque de disponibilidad ya existente."
                );
            }
        }

        if (!verificarDisponibilidadCuidador(cuidadorId, nuevoHorario)) {
            throw new SolapamientoHorarioException(
                    "El horario seleccionado choca con un servicio ya agendado y aceptado."
            );
        }

        cuidador.addHorarioDisponible(nuevoHorario);
        usuarioRepository.save(cuidador);

        return nuevoHorario;
    }

    public boolean verificarDisponibilidadCuidador(String cuidadorId, Horario nuevoHorario) {

        if (cuidadorId == null || cuidadorId.isBlank()) {
            throw new ReglaNegocioException("El ID del cuidador es obligatorio.");
        }

        if (nuevoHorario == null) {
            throw new ReglaNegocioException("El horario es obligatorio.");
        }

        List<Solicitud> solicitudesCuidador = solicitudRepository.findAll().stream()
                .filter(s -> cuidadorId.equals(s.getCuidadorId()))
                .filter(s -> s.getEstado() != null)
                .filter(s -> "ACEPTADA".equals(s.getEstado().name()))
                .toList();

        for (Solicitud solicitud : solicitudesCuidador) {
            if (solicitud.getHorario() != null && solicitud.getHorario().seSolapaCon(nuevoHorario)) {
                return false;
            }
        }

        return true;
    }
}